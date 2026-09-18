# Reproducible EMF JSON measurements

This optional harness loads an external SysML binary export and exercises
`JsonResourceImpl` with Sirius Web's UUID-adapter ID representation, dynamic
instance option, schema locations and namespace listener. It adds no runtime
dependencies to emfjson. The corpus and recordings stay outside Git.

## Build and run

Use Java 21. Build the reference revision with `mvn verify` and retain its
`org.eclipse.sirius.emfjson/target/classes` directory before building changes.
Provide a text file containing the colon-separated SysML model/logic runtime
classpath (including their dependencies). Relative entries resolve against
the invoking working directory. Project-resolved EMF/Gson dependencies take
precedence over shaded copies in the external classpath.

The acceptance protocol for this campaign is one fresh JVM per variant, two
warmups and ten measured iterations. Pass those settings explicitly:

```sh
FORKS=1 WARMUP=2 ITERATIONS=10 BENCHMARK_JAVA_HOME=/path/to/jdk21 \
  bash benchmarks/run.sh \
  /path/to/apollo-11.bin /path/to/dependency-classpath.txt \
  target/performance/comparison /path/to/reference/classes \
  /path/to/candidate/classes
```

All functional validation remains enabled. This deliberately short acceptance
protocol has limited statistical confidence: two warmups may not stabilize
the JVM, and one fork cannot establish between-JVM variability. Report results
as observations from one JVM per variant; ten iterations are not ten independent
JVM replicas, and do not support a confidence interval across JVMs.

For even shorter exploratory screening, use `ITERATIONS=5` with the same one
fork and two warmups. Screening does not replace the ten-measurement acceptance
run. Keep the 2 GiB fixed heap and record any changes to `FORKS`, `WARMUP`,
`ITERATIONS` or `HEAP`. Historical longer campaigns retain their own protocols.
Use `PROFILE=1` for additional, separately recorded JFR runs; never include
their CSV files in timing comparisons. Do not run builds or other benchmarks
concurrently. Increase warmup if measurements have not stabilized.

Additional candidate class directories may follow the first candidate. Output
names are `baseline`, `candidate`, `candidate2`, and so on; the configuration
records their paths and each compiled variant is fingerprinted. Variant order
is reversed on even forks. This supports cumulative attribution without
running identical intermediate variants again in separate pairwise campaigns.

The output directory contains golden JSON, a persistent-model fingerprint,
runtime provenance, raw CSVs, validation logs and optional JFR recordings.
Use a fresh output directory for each experiment. Failed validation exits
nonzero; do not summarize incomplete runs as successful measurements.

## Measurement boundaries

- Save includes `resource.save` and the final `toByteArray` copy. Source
  preparation and binary loading are excluded.
- Load includes creation of a fresh resource/resource set and `resource.load`
  from immutable golden JSON bytes.
- Both paths use actual output and check the final result outside timing.
  Saving must reproduce reference bytes exactly. Loading checks persistent
  attributes, ordered references, containment, unsettable state and IDs.
- These are codec measurements, not PostgreSQL or complete editing-context
  opening timings. Migration participants and cross-reference indexes are
  not included in these default measurements.

JFR starts before warmup. The custom `emfjson.Measurement` event bounds the
measured interval; include it when exporting samples. This avoids attributing
startup instrumentation and accumulated pre-recording allocations to the
codec. Sampling does not replace the exact per-thread allocation counters.
Separate profiling runs request up to 1,000 allocation samples per second
to improve attribution during short campaigns; they are not timing results.

Summarize unprofiled forks with `python3.13 benchmarks/summarize.py
target/performance/comparison/*-fork*.csv`. Use `--json` for machine-readable
results. The analysis treats JVMs as independent replicas and keeps separate
campaign directories separate. Empty or invalid CSVs fail analysis.

`OPERATIONS='tree emit parse materialize save-string save-string-sized
save-characters load-string load-characters'` selects additional diagnostics:
DOM construction, buffered emission of a retained DOM, JSON parsing,
construction of EMF objects from a retained DOM, and the byte/character
conversions at the document boundary. `save-string-sized` uses the exact
reference byte length as its initial output capacity. The character variants
call the ordinary Gson tree adapters but bypass the EMF `InputStream` and
`OutputStream` lifecycle, including `ResourceHandler`; they are opportunity
bounds, not compatible replacement APIs. These phases are independent
experiments, not additive timings: retained trees change live memory, and
direct materialization omits the `Resource.load` lifecycle. Their ratios are
not guaranteed gains from a hypothetical streaming implementation.

`OPERATIONS='save binary-save'` compares JSON saving with EMF's native binary
path through `XMIResourceImpl` and `XMLResource.OPTION_BINARY`. Both operations
include `ByteArrayOutputStream.toByteArray`; the CSV records each format's real
output size. The binary result is reloaded and checked with the same persistent
model and ID fingerprint. This is a codec diagnostic, not evidence that the
binary format preserves JSON processors, handlers, migrations or database
compatibility.

`OPERATIONS='save save-streaming'` compares the ordinary save with the opt-in
`OPTION_STREAMING` path. Streaming writes `content` first and deliberately
omits object-tree callbacks. Its output must have this ordering, equal the
baseline JSON tree, and reload with the same persistent-model/ID fingerprint;
the ordinary save still requires byte equality. This diagnostic must run
against a revision supporting the option. It does not demonstrate compatibility
with arbitrary application callbacks or migration participants.

```sh
/path/to/jdk21/bin/jfr print --json \
  --events emfjson.Measurement,jdk.ExecutionSample,jdk.NativeMethodSample,jdk.ObjectAllocationSample \
  target/performance/comparison/profile-baseline-save.jfr
```

Save this generated JSON to an artifact file and pass it to `summarize.py`.
The analyzer requires the measurement interval and drops its first allocation
sample per thread because that weight can include pre-interval allocations.
CPU samples and weighted allocations are estimates; inclusive costs overlap.

Apollo's libraries are merged into one document. Use the ordinary regression
suite and growing-list tests to cover interdocument references and extension
callbacks; do not extrapolate Apollo results to every EMF graph shape.
