# Issue 19 reproduction corpus

`large_same_document_references.ecore` is an Ecore resource designed to make
the repeated classification in `GsonEObjectSerializer.docKindMany` visible.

The generated file is 283,271 bytes and has SHA-256
`8cf3574b24d20ddd821bb16c84bc3b9cde8096d3bdc7d495e176244898216918`.

The resource contains:

- one package;
- 4,096 empty `EClass` instances named `Base0000` through `Base4095`;
- one `Aggregate` class whose `eSuperTypes` list contains all 4,096 base
  classes.

This gives 4,098 EObjects and 4,096 populated non-containment references. All
targets are in the same resource and none is a proxy. When serializing
`Aggregate`, the current implementation calls `docKindMany` once per value,
and each call scans the complete list again. The corpus therefore drives
`4,096 × 4,096 = 16,777,216` same-document classification checks for one
reference list, while preserving the semantics of the issue reporter's
same-document workload.

Regenerate the deterministic corpus with:

```bash
python3.13 benchmarks/issue-19/generate_dataset.py
```

The next step is to load this `.ecore` resource with EMF's Ecore package and
measure `JsonResourceImpl.toJson` before and after computing the document kind
once per reference list.

As a baseline measurement on 2026-09-22 (Temurin 21.0.6+7, two warmups and
ten measurements, current `master`), serialization produced 664,111 UTF-8
bytes and took 356--381 ms per operation (median 367.5 ms). This is a
single-workstation screening result, not an acceptance benchmark.
