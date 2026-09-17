import json
import tempfile
import unittest
from pathlib import Path

from summarize import csv_report, describe, duration_ns, jfr_report, timestamp_ns


class SummaryTest(unittest.TestCase):
    def test_rejects_failed_or_malformed_forks(self):
        header = "operation,iteration,wall_ns,cpu_ns,allocated_bytes,output_bytes\n"
        invalid = ["", "error\nbenchmark failed\n", header,
                   "operation,phase,wall_ns\nsave,warmup,10\n",
                   header + "save,0,10\n", header + "save,0,10,20,30,40,50\n",
                   header + ",0,10,20,30,40\n", header + "save,0,,20,30,40\n",
                   header + "save,0,nan,20,30,40\n", header + "save,0,invalid,20,30,40\n"]
        with tempfile.TemporaryDirectory() as directory:
            valid = Path(directory) / "baseline-save-fork1.csv"
            failed = Path(directory) / "baseline-save-fork2.csv"
            valid.write_text(header + "save,0,10,-1,20,30\n")
            for content in invalid:
                with self.subTest(content=content):
                    failed.write_text(content)
                    with self.assertRaises(ValueError):
                        csv_report([valid, failed])

    def test_independent_forks_and_recursive_samples(self):
        self.assertEqual(describe([[1, 1, 1], [9]])["median"], 5)
        self.assertIsNone(describe([[1, 2]])["fork_bootstrap_95_ci"])
        self.assertEqual(describe([[1], [9]]), describe([[1], [9]]))
        with tempfile.TemporaryDirectory() as directory:
            first = Path(directory) / "baseline-save-fork1.csv"
            second = Path(directory) / "baseline-save-fork2.csv"
            header = "operation,iteration,wall_ns,cpu_ns,allocated_bytes,output_bytes\n"
            first.write_text(header + "save,0,10,-1,20,30\nsave,1,10,-1,20,30\n")
            second.write_text(header + "save,0,30,-1,40,30\n")
            stats = csv_report([first, second])[f"{Path(directory).resolve()}/baseline-save/save"]
            self.assertEqual(stats["wall_ns"]["median"], 20)
            self.assertEqual(stats["wall_ns"]["forks"], 2)
            self.assertNotIn("cpu_ns", stats)
            campaign = Path(directory) / "other-campaign"
            campaign.mkdir()
            other = campaign / first.name
            other.write_text(header + "save,0,100,-1,200,30\n")
            campaigns = csv_report([first, second, other])
            self.assertEqual(len(campaigns), 2)
            self.assertEqual(campaigns[f"{campaign.resolve()}/baseline-save/save"]["wall_ns"]["median"], 100)
            self.assertEqual(campaigns[f"{Path(directory).resolve()}/baseline-save/save"]["wall_ns"]["forks"], 2)
            frame = {"method": {"type": {"name": "a/B"}, "name": "run", "descriptor": "()V"}}
            values = {"eventThread": {"javaName": "main"}, "weight": 100,
                      "stackTrace": {"frames": [frame, frame]}}
            recording = Path(directory) / "samples.json"
            events = [{"type": "jdk.ObjectAllocationSample", "values": dict(values, startTime=f"2026-01-01T00:00:0{second}Z")}
                      for second in (0, 1, 2, 3)]
            recording.write_text(json.dumps({"recording": {"events": events}}))
            with self.assertRaisesRegex(ValueError, "no emfjson.Measurement"):
                jfr_report([recording], "main", 10)
            events.append({"type": "emfjson.Measurement", "values": {
                "startTime": "2026-01-01T00:00:01Z", "duration": "PT2S"}})
            recording.write_text(json.dumps({"recording": {"events": list(reversed(events))}}))
            allocation = jfr_report([recording], "main", 10)[str(recording)]["jdk.ObjectAllocationSample"]
            self.assertEqual(allocation["events"], 1)  # Outside events and first in-interval weight excluded.
            self.assertEqual(allocation["self"][0]["weight"], 100)
            self.assertEqual(allocation["inclusive"][0]["percent"], 100)
            self.assertEqual(jfr_report([recording], "other", 10)[str(recording)], {})
        self.assertEqual(duration_ns("PT1M0.000000001S"), 60_000_000_001)
        self.assertEqual(timestamp_ns("2026-01-01T01:00:00.000000001+01:00")
                         - timestamp_ns("2026-01-01T00:00:00Z"), 1)


if __name__ == "__main__":
    unittest.main()
