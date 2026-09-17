#!/usr/bin/env python3.13
"""Summarize one CSV per JVM or `jfr print --json` exports, using only stdlib."""
import argparse
import csv
import json
import math
import random
import re
import statistics
from collections import Counter, defaultdict
from datetime import datetime
from decimal import Decimal
from pathlib import Path


def percentile(values, fraction):
    ordered = sorted(values)
    position = (len(ordered) - 1) * fraction
    lower = int(position)
    upper = min(lower + 1, len(ordered) - 1)
    return ordered[lower] + (ordered[upper] - ordered[lower]) * (position - lower)


def describe(forks):
    """Bootstrap independent JVM medians, not correlated iterations as replicas."""
    medians = [statistics.median(values) for values in forks]
    rng = random.Random(0)
    interval = None
    if len(medians) > 1:
        samples = [statistics.median(rng.choices(medians, k=len(medians))) for _ in range(10000)]
        interval = [percentile(samples, 0.025), percentile(samples, 0.975)]
    pooled = [value for values in forks for value in values]
    return {"median": statistics.median(medians), "fork_medians": medians,
            "iteration_iqr": [percentile(pooled, 0.25), percentile(pooled, 0.75)],
            "fork_bootstrap_95_ci": interval, "forks": len(forks), "iterations": len(pooled)}


def csv_report(paths):
    groups = defaultdict(lambda: defaultdict(lambda: defaultdict(list)))
    for path in paths:
        variant = re.sub(r"[-_]?(?:fork[-_]?)?\d+$", "", path.stem)
        with path.open(newline="") as stream:
            reader = csv.DictReader(stream)
            fields = reader.fieldnames or []
            metrics = [column for column in fields if column.endswith(("_ns", "_bytes", "_count", "_ms"))]
            if not {"operation", "op"}.intersection(fields) or not metrics or len(fields) != len(set(fields)):
                raise ValueError(f"{path}: invalid benchmark CSV header")
            measured_rows = 0
            for row in reader:
                if None in row or None in row.values():
                    raise ValueError(f"{path}:{reader.line_num}: incomplete or malformed CSV row")
                if row.get("phase", "measure") not in ("measure", "measurement"):
                    continue
                operation = row.get("operation", row.get("op"))
                if not operation or any(not row[column] for column in metrics):
                    raise ValueError(f"{path}:{reader.line_num}: missing operation or metric")
                measured_rows += 1
                for column in metrics:
                    number = float(row[column])
                    if not math.isfinite(number):
                        raise ValueError(f"{path}:{reader.line_num}: non-finite metric {column}")
                    if number >= 0:  # Negative JVM counters denote unavailable measurements.
                        groups[f"{path.parent.resolve()}/{variant}/{operation}"][column][str(path.resolve())].append(number)
            if not measured_rows:
                raise ValueError(f"{path}: no measured CSV rows; the benchmark may have failed")
    return {group: {metric: describe(list(forks.values())) for metric, forks in metrics.items()}
            for group, metrics in sorted(groups.items())}


def method_name(frame):
    method = frame.get("method") or {}
    owner = (method.get("type") or {}).get("name", "?").replace("/", ".")
    return owner + "." + method.get("name", "?") + method.get("descriptor", "")


def timestamp_ns(value):
    match = re.fullmatch(r"(.*T\d\d:\d\d:\d\d)(?:\.(\d+))?(Z|[+-]\d\d:\d\d)", value)
    base, fraction, zone = match.groups()
    return int(datetime.fromisoformat(base + zone).timestamp()) * 10**9 + int((fraction or "").ljust(9, "0"))


def duration_ns(value):
    hours, minutes, seconds = re.fullmatch(r"PT(?:(\d+)H)?(?:(\d+)M)?(?:([\d.]+)S)?", value).groups()
    return int((Decimal(hours or 0) * 3600 + Decimal(minutes or 0) * 60 + Decimal(seconds or 0)) * 10**9)


def jfr_report(paths, thread, limit, unscoped=False):
    reports = {}
    for path in paths:
        with path.open() as stream:
            events = json.load(stream)["recording"]["events"]
        intervals = []
        for event in events:
            if event["type"] == "emfjson.Measurement":
                values = event["values"]
                start = timestamp_ns(values["startTime"])
                intervals.append((start, start + duration_ns(values["duration"])))
        if not intervals and not unscoped:
            raise ValueError(f"{path}: no emfjson.Measurement event; export it or use --unscoped for exploration")
        first_allocations = set()
        counts = Counter()
        totals = Counter()
        stacks_missing = Counter()
        self_cost = defaultdict(Counter)
        inclusive = defaultdict(Counter)
        for event in sorted(events, key=lambda item: timestamp_ns(item["values"]["startTime"])):
            kind = event["type"]
            if kind not in ("jdk.ExecutionSample", "jdk.NativeMethodSample", "jdk.ObjectAllocationSample"):
                continue
            values = event["values"]
            event_thread = values.get("sampledThread") or values.get("eventThread") or {}
            if thread and event_thread.get("javaName") != thread:
                continue
            interval = next((index for index, (start, end) in enumerate(intervals)
                             if start <= timestamp_ns(values["startTime"]) < end), None)
            if intervals and interval is None:
                continue
            if kind == "jdk.ObjectAllocationSample":
                key = (interval, event_thread.get("javaThreadId", event_thread.get("javaName")))
                if key not in first_allocations:
                    first_allocations.add(key)
                    continue  # First weight may include allocations preceding the interval.
            weight = values["weight"] if kind == "jdk.ObjectAllocationSample" else 1
            counts[kind] += 1
            totals[kind] += weight
            frames = (values.get("stackTrace") or {}).get("frames") or []
            if not frames:
                stacks_missing[kind] += weight
                continue
            methods = [method_name(frame) for frame in frames]
            self_cost[kind][methods[0]] += weight
            for method in set(methods):  # Recursion contributes once per sampled stack.
                inclusive[kind][method] += weight
        reports[str(path)] = {
            kind: {"events": counts[kind], "total_weight": total,
                   "unit": "estimated allocated bytes" if kind == "jdk.ObjectAllocationSample" else "samples",
                   "missing_stack_weight": stacks_missing[kind],
                   "self": [{"method": name, "weight": weight, "percent": weight * 100 / total}
                            for name, weight in self_cost[kind].most_common(limit)],
                   "inclusive": [{"method": name, "weight": weight, "percent": weight * 100 / total}
                                 for name, weight in inclusive[kind].most_common(limit)]}
            for kind, total in totals.items() if total}
    return reports


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("files", nargs="+", type=Path)
    parser.add_argument("--json", action="store_true", help="Machine-readable report")
    parser.add_argument("--thread", default="main", help="JFR Java thread name; empty string selects all")
    parser.add_argument("--top", type=int, default=15)
    parser.add_argument("--unscoped", action="store_true", help="Allow exploratory JFR exports without measurement events")
    args = parser.parse_args()
    if any(path.suffix not in (".csv", ".json") for path in args.files):
        parser.error("Inputs must be CSV files or JFR JSON exports")
    result = {
        "notes": ["CSV: one file per JVM; bootstrap of fork medians (seed 0, 10000 resamples).",
                  "Single-fork confidence intervals are unavailable; few forks give coarse intervals.",
                  "JFR: sampled estimates, not exact CPU durations or allocation counts; inclusive percentages overlap.",
                  "JFR uses emfjson.Measurement intervals; --unscoped permits exploratory whole recordings.",
                  "First allocation sample per thread/interval discarded: its weight can span the boundary; estimates undercount."],
        "csv": csv_report([path for path in args.files if path.suffix == ".csv"]),
        "jfr": jfr_report([path for path in args.files if path.suffix == ".json"], args.thread, args.top, args.unscoped)}
    if args.json:
        print(json.dumps(result, indent=2))
        return
    print("\n".join(result["notes"]))
    for group, metrics in result["csv"].items():
        print(f"\n{group}")
        for metric, stats in metrics.items():
            print(f"  {metric}: median={stats['median']:,.1f}; IQR={stats['iteration_iqr']}; "
                  f"95% CI={stats['fork_bootstrap_95_ci']}; forks={stats['forks']}; n={stats['iterations']}")
    for path, kinds in result["jfr"].items():
        print(f"\n{path} (thread={args.thread or 'all'})")
        for kind, stats in kinds.items():
            print(f"  {kind}: {stats['events']} events; {stats['total_weight']:,} {stats['unit']}")
            for attribution in ("self", "inclusive"):
                print(f"    {attribution}")
                for row in stats[attribution]:
                    print(f"      {row['percent']:6.2f}% {row['method']}")


if __name__ == "__main__":
    main()
