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

```sh
BENCHMARK_JAVA_HOME=/path/to/jdk21 bash benchmarks/run.sh \
  /path/to/apollo-11.bin /path/to/dependency-classpath.txt \
  target/performance/comparison /path/to/reference/classes \
  /path/to/candidate/classes
```

Defaults: five JVM forks, ten warmup iterations, thirty measured iterations,
2 GiB fixed heap. Override with `FORKS`, `WARMUP`, `ITERATIONS`, `HEAP`.
Use `PROFILE=1` for additional, separately recorded JFR runs; never include
their CSV files in timing comparisons. Do not run builds or other benchmarks
concurrently. Increase warmup if measurements have not stabilized.

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

```sh
/path/to/jdk21/bin/jfr print --json \
  --events emfjson.Measurement,jdk.ExecutionSample,jdk.NativeMethodSample,jdk.ObjectAllocationSample \
  target/performance/comparison/profile-baseline-save.jfr
```

Apollo's libraries are merged into one document. Use the ordinary regression
suite and growing-list tests to cover interdocument references and extension
callbacks; do not extrapolate Apollo results to every EMF graph shape.
