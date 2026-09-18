# EMF JSON performance investigation

## Reproduction boundary

Reference: emfjson `4ab919838f3f55104b7b4612d595f6c2af2da7d1`.
The reference passes `mvn verify` with Temurin 21.0.6+7.
Production dependencies and JSON contracts remain unchanged.

The external Apollo 11 binary contains 138,556 EObjects, including the
project and standard libraries merged into one resource. Its SHA-256 is
`b2e69adb4c6c6da331f22509d0801942bb258d0eb3c3a7977919cfb60b7fa8e6`.
It primarily exercises references within one document; it cannot alone
establish performance for references between documents.

Measurements use a real `JsonResourceImpl`, matching the resource created by
Sirius Web master at `6843419ef5474303d6de347b44062f2a3d59e42e`.
Binary loading and SysML initialization are preparation, not part of JSON
timings. IDs are preserved from the input. The benchmark's UUID adapters
reproduce Sirius Web's ID representation.

Workstation: Intel Core Ultra 7 155H, eight online logical CPUs, 93 GiB RAM,
Linux 6.4.0-150600.21-default. JVM: Temurin 21.0.6+7, G1, 2 GiB fixed heap.
Unprofiled measurements and JFR recordings run separately. CPU and allocation
profiles are sampled estimates; inclusive stack percentages overlap.

The aborted `final-comparison` campaign contains the subsequently rejected
reference-classification cache. Its files are exploratory artifacts, **not
acceptance data for the retained implementation**. The corrected R4 campaign
`compatible-final-comparison` was also interrupted when a quieter workstation
window became available; it is exploratory, not a completed five-fork run.
Phase attribution uses fresh `quiet-save` and `quiet-load` campaigns. Do not
combine measurements across these campaigns into one population.

The machine's CPU governor is `powersave`; frequency and desktop activity are
not controlled by the harness. The user reduced workstation activity before
the quiet campaigns. This reduces interference, but does not establish a
dedicated benchmark machine or justify disregarding uncertainty intervals.

## Experiment ledger

| Hypothesis | Evidence to collect | Compatibility constraint | Result |
| --- | --- | --- | --- |
| Repeated classification of a reference list is quadratic | Growing-list counts confirm quadratic work, but Apollo has 738 such lists and every one is a singleton; scoped JFR attributes no CPU sample and 0.0194% of sampled allocations to `docKindMany` | Resource fragment and ID-manager callbacks can change reference ownership between elements | No change: there is no repeated scan on the target corpus, and a generic cache changes observable callback behavior |
| Unbuffered JSON character writes allocate temporary arrays | Five-fork save comparison and final one-fork comparison below; `StreamEncoder` CPU/allocation stacks | Preserve stream visibility for resource handlers | Large measured allocation reduction |
| Repeated feature dispatch costs remain after excluded-feature skipping | Cross-ordered 2+10 save comparisons | Cache only frozen standard declaring classes; preserve custom metadata and callbacks | Accepted: three final comparisons observed 0.28–7.39% lower save wall time and 0.42–9.91% lower CPU |
| Reference insertion or attachment dominates load | Five-fork load comparison below | Preserve uniqueness, opposites and notifications | Containment shortcut rejected: no convincing gain |
| Avoiding untyped ID splitting reduces load allocation | Five-fork load comparison below | Preserve whitespace and processor ordering | Split shortcut rejected: no measured benefit over its base |
| Per-write locking remains after character buffering | Cross-ordered 2+10 save comparisons and a post-change JFR | Preserve encoding, close/flush failures and resource-handler stream visibility | Accepted: save wall time fell by 17.3% in the final acceptance order; the reverse order observed 19.2% |
| Default Gson arrays over-allocate short EMF reference lists | Cross-ordered 2+10 save comparisons | Preserve list traversal and callback order; use capacity only | Accepted: allocation fell by 1.93% in the final order and 1.10% in reverse order |
| Gson tree construction dominates remaining allocations | Allocation profiles and cross-ordered streaming comparisons | Existing callbacks accept complete JSON trees | Accepted opt-in mode: about 32% fewer allocated bytes; default contract unchanged |
| EMF binary serialization provides an order-of-magnitude reference | Cross-ordered 2+10 codec comparisons | Compare equivalent persistent state and extrinsic IDs, not format compatibility | Binary was 25–27% slower, allocated 60.8% less and produced 47.7% fewer bytes than optimized JSON |

## Five-fork phase measurements

Each row below represents five independent JVM forks on the same Apollo
corpus, using the codec-only boundary and fixed JVM configuration above.
Elapsed and CPU values are milliseconds per operation; allocations are bytes
per operation. Rows identify cumulative experimental states, not interchangeable
measurements of the final source. Timing variation between forks prevents
assigning every small difference to the most recent edit.

### Save: `quiet-save`

| Experimental state | Elapsed ms | CPU ms | Allocated bytes |
| --- | ---: | ---: | ---: |
| Reference R0 | 520.8646 | 497.124 | 763,837,696 |
| Buffer only | 445.3462 | 436.993 | 271,667,984 |
| IDs plus buffer | 447.6491 | 442.083 | 271,667,984 |
| Frozen-feature prototype plus preceding changes | 382.2853 | 376.256 | 269,498,064 |

Buffering reduced observed allocation by 492,169,712 bytes per save, about
64.4%, and the elapsed point estimate by about 14.5%. ID changes added no
save-allocation benefit in this comparison and no demonstrated timing gain.
The frozen-feature prototype's incremental point estimate was about 14.6%
less elapsed time than IDs plus buffer, with 2,169,920 fewer allocated bytes.
These are phase measurements, not confidence bounds on the final patch.

After this campaign, review restricted feature caching to the exact
`EClassImpl` class and restored iterator traversal for other implementations.
The table therefore documents the earlier prototype; the final guard
refinement is measured separately in `accepted-save` below. No tenfold gain
is demonstrated.

### Load: `quiet-load`

| Experimental state | Elapsed ms | Elapsed interval ms | CPU ms | Allocated bytes |
| --- | ---: | --- | ---: | ---: |
| Reference R0 | 471.4969 | [449.72, 514.54] | 378.560 | 417,254,720 |
| Containment shortcut | 509.1961 | [459.88, 516.33] | 424.868 | 417,254,720 |
| IDs on containment shortcut | 523.1045 | [454.30, 591.90] | 425.568 | 409,564,728 |
| Split shortcut | 520.0460 | [513.42, 557.14] | 429.848 | 412,824,808 |

Timing intervals overlap. Containment shows no allocation reduction and no
convincing elapsed-time benefit. Splitting avoidance shows no convincing
timing improvement over its cumulative base and allocates more in this
comparison. Neither candidate is retained. ID changes reduced allocation
by 7,689,992 bytes on the containment base, but that is not an isolated
comparison against the unchanged loader.

### Isolated ID comparison: `id-load`

This completed comparison used three forks, ten warmups and thirty measured
operations per fork. It isolates the ID changes from the rejected containment
and split trials; the loader source is unchanged from R0.

| Experimental state | Elapsed ms | Elapsed interval ms | CPU ms | Allocated bytes |
| --- | ---: | --- | ---: | ---: |
| Reference R0 | 538.2586 | [502.8157, 558.6354] | 448.4801 | 417,254,720 |
| I1: repeated-ID map guard only | 515.3263 | [514.2783, 537.4188] | 426.7521 | 409,564,720 |
| I2: additionally cache manager selection | 514.2404 | [510.1629, 546.1433] | 422.5353 | 412,824,808 |

The map guard saves 7,690,000 allocated bytes per load, about 1.84%, and is
retained for that measured reduction. Its elapsed point estimate is 4.26%
lower, but overlapping intervals do not establish an elapsed-time gain.
Caching the manager adds only a 0.2% elapsed point-estimate difference, with
no confirmed timing benefit and no allocation improvement over I1. The
cached field was removed; each lookup continues to select the manager as
before. No runtime benefit is claimed for that rejected experiment.

### Repeated EClass lexical components

Apollo contains 138,556 `eClass` values but only 131 distinct lexical class
names. The loader now caches only each name's namespace prefix and local class
name for the duration of one load. It still queries the package registry and
resolves the EClassifier for every EObject, so handler-driven registry changes
remain observable.

Two cross-ordered comparisons used one fresh JVM per variant, two warmups and
ten measurements:

| Order | Variant | Wall ms | CPU ms | Allocated bytes |
| --- | --- | ---: | ---: | ---: |
| Previous then cache | Previous | 436.896 | 385.122 | 412,899,092 |
| Previous then cache | Cache | 452.168 | 389.383 | 398,206,320 |
| Cache then previous | Previous | 453.284 | 390.106 | 412,833,816 |
| Cache then previous | Cache | 450.603 | 391.606 | 398,206,320 |

Allocation fell by 14.63–14.69 MB per load, or 3.54–3.56%. Wall time changed
by +3.50% and -0.59%; CPU changed by +1.11% and +0.38%, so no timing gain is
claimed. Model/ID validation and package-registry mutation tests passed. These
are single-JVM codec observations without a between-JVM confidence interval.

The accepted source consists of buffering, the repeated-ID map guard, and
frozen-feature skipping with the exact-class guard and iterator fallback.
`accepted-save` compares R0, buffer plus I1 without feature skipping, and
the full accepted variant, using one fork, ten warmups and thirty measured
operations. Its completed results are below. The earlier five-fork feature
prototype supports the mechanism but is not a five-fork measurement of the
final guard implementation.

### Final source: `accepted-save`

| State | Elapsed ms | CPU ms | Allocated bytes |
| --- | ---: | ---: | ---: |
| Reference R0 | 442.562645 | 425.262126 | 763,837,712 |
| Buffer plus repeated-ID guard, no feature skipping | 423.809968 | 405.149759 | 271,668,000 |
| Final accepted implementation | 325.697065 | 322.136963 | 267,281,168 |

The final implementation reduces the elapsed point estimate by 26.407% and
allocated bytes by 65.008% against R0. Feature skipping contributes an
incremental 23.15% elapsed reduction and 1.61% allocation reduction against
buffer plus the repeated-ID guard in this run. These timings are provisional:
there is only one JVM fork per state and no confidence interval. Thirty
operations within one JVM do not substitute for independent forks. The
different baseline times across campaigns also show why their populations
must remain separate. No tenfold improvement is demonstrated.

The accepted compiled snapshot in `target/performance/accepted-classes`
combines the reviewed final serializer with I1 resource classes; the resource
source was compared and found identical to I1. The final module's clean
`verify` passed. Comparing its complete `target/classes` directory against
`target/performance/accepted-classes` with `diff -qr` returned zero, verifying
that the measured snapshot exactly matches the final compiled classes.
The quiet campaign finished at 14:30:33 UTC, before the 14:30:53 UTC deadline
when the workstation was needed for Zoom. Measurements from different
activity windows must not be silently pooled. Final profiles have been
recorded; their attribution analysis and application integration results
remain pending.

### Unsynchronized private writer

After buffering removed repeated encoder allocation, the accepted profile still
attributed 16.09% of save CPU samples directly to `BufferedWriter.implWrite`
and 10.32% to `ReentrantLock.Sync.lock`. `JsonResourceImpl` uses its writer on
one save thread, so the candidate replaces `BufferedWriter` with a private
8 KiB character buffer whose methods do not lock. Encoding remains delegated
to `OutputStreamWriter`. Saves with a resource handler retain the previous
unbuffered writer because `postSave` can observe that stream boundary.

The final acceptance comparison used one fresh JVM per variant, two warmups
and ten measurements on the 138,556-object Apollo corpus. It ran the previously
accepted classes first and the writer candidate second:

| State | Elapsed ms | CPU ms | Allocated bytes |
| --- | ---: | ---: | ---: |
| Previous accepted implementation | 292.290594 | 292.269401 | 269,498,064 |
| Unsynchronized private writer | 241.784749 | 239.020429 | 267,281,376 |

The elapsed median fell by 17.28% and CPU by 18.22%. A separate reverse-order
acceptance run observed 19.17% less elapsed time and 18.75% less CPU. The two
runs disagree on a small allocation change (approximately plus or minus 0.8%),
so no allocation improvement is attributed to this writer. Both comparisons
produced the same 25,347,610 JSON bytes and passed the harness round-trip checks.
They remain single-JVM observations without a between-JVM confidence interval.

A scoped post-change JFR no longer ranks `BufferedWriter.implWrite` or
`ReentrantLock` among the sampled save costs. The remaining save work is led by
Gson tree construction/emission and EMF feature access. The complete Maven
verification passes 528 tests, including byte-exact UTF-8/UTF-16LE, indentation,
flush-failure and resource-handler boundary checks.

### Pre-sized reference arrays

The Apollo JSON contains 93,452 arrays; 69,253 contain one element and 93,290
contain at most ten. Gson's default `JsonArray` backing list grows to capacity
ten on its first addition. The candidate supplies known top-level and EMF
reference-list sizes to `JsonArray` while retaining the same iteration and
element callbacks.

The final one-JVM, two-warmup, ten-measurement acceptance order observed save
wall time fall from 240.771 ms to 234.573 ms (-2.57%), CPU from 239.122 ms to
232.700 ms (-2.69%), and allocation from 269,498,272 to 264,304,168 bytes
(-1.93%). In reverse order, wall and CPU fell by 1.29% and allocation from
269,498,272 to 266,521,064 bytes (-1.10%). An earlier five-measurement screening
had noisier timing in the opposite direction, so the small timing improvement
is not presented as established beyond these two single-JVM acceptance runs.
The allocation reduction was observed in all three orders.

### Frozen feature serialization plans

The earlier frozen-feature cache only skipped transient and derived features.
The expanded session-scoped plan also classifies standard attributes lazily by
value family and references by cardinality and containment. The hot loop dispatches
directly to the existing serialization methods; feature filtering, value access,
QName computation, recursive traversal and callbacks are unchanged. Lazy attribute
classification retains the previous timing of `getEType`, including proxy
resolution. Classification is restricted to exact standard features whose
declaring `EClassImpl` is frozen. Inherited features from mutable external
supertypes and custom feature classes retain generic dispatch.

The final acceptance comparisons used one fresh JVM per variant, two warmups and
ten measurements on the 138,556-object Apollo corpus:

| Order | Previous wall ms | Plan wall ms | Previous CPU ms | Plan CPU ms |
| --- | ---: | ---: | ---: | ---: |
| Previous then plan | 234.042 | 233.392 (-0.28%) | 233.526 | 232.557 (-0.42%) |
| Plan then previous | 242.264 | 236.592 (-2.34%) | 240.957 | 236.512 (-1.84%) |
| Confirmation | 245.869 | 227.710 (-7.39%) | 245.835 | 221.465 (-9.91%) |

The cross-ordered final runs observed allocation fall by 0.83%, from approximately
266,521,000 to 264,320,000 bytes, while confirmation observed a 15,812-byte
increase (0.006%). The two modes differ by approximately 2,216,900 bytes, or
16 bytes for each Apollo EObject, suggesting but not proving a JVM optimization
regime. No robust allocation reduction is therefore attributed to the plan.
Every run produced the same 25,347,610 JSON bytes and passed model validation.
These remain separate single-JVM observations without a between-JVM confidence
interval; the 0.28% result in particular is within ordinary run-to-run noise.

### Optional streaming saves

`OPTION_STREAMING` avoids the full object/containment tree for frozen standard
metadata. It writes content before headers and skips object-tree callbacks;
see ADR-001 for restrictions and fallback behavior. This option is not enabled
by default and has not been adopted in Sirius Web by this contribution.

Two cross-ordered Apollo runs used one fresh JVM per mode, two warmups and ten
measurements (Java 21, G1, fixed 2 GiB heap):

| Order | Mode | Wall ms | CPU ms | Allocated bytes |
| --- | --- | ---: | ---: | ---: |
| Tree then stream | Tree | 238.376 | 238.039 | 266,537,240 |
| Tree then stream | Stream | 209.595 | 206.270 | 180,469,272 |
| Stream then tree | Tree | 241.949 | 240.863 | 264,320,344 |
| Stream then tree | Stream | 215.639 | 210.689 | 180,469,272 |

Streaming reduced allocations by 31.72–32.29%, wall time by 10.87–12.07% and
CPU by 12.53–13.35% in these single-JVM observations. Both modes produced
25,347,610 bytes. Streaming passed complete JSON-tree equality and persistent
model/ID validation; ordinary output remained byte-identical to the previous
revision. There is no between-JVM confidence interval or end-to-end claim.
Artifacts: `target/performance/streaming-acceptance` and its `-reversed` run.

A separate scoped JFR (1,758 allocation samples) attributed 51.02% of remaining
sampled bytes to output-array growth/copying, including 14.10% to the final
`toByteArray`, and 22.92% inclusively to SysML adapter lookup. These estimates
are not exact component counters; inclusive stacks can overlap.

The next change writes single string/character, boolean and numeric attributes
directly, preserving their existing value-type checks and omission behavior.
It removes temporary `JsonPrimitive` wrappers without a cache. Two cross-ordered
1-JVM/2-warmup/10-measurement comparisons observed allocations fall from
182,686,152 / 182,686,168 to 180,059,288 bytes: 2.63 MB or 1.44% less per save.
Wall time changed by +0.64% in forward order and -1.17% in reverse order; CPU
changed by +0.17% and -1.85%. No timing gain is claimed for this small change.
JSON-tree equality, 25,347,610-byte output size and model/ID validation passed.
Artifacts: `target/performance/streaming-scalars-acceptance` and `-reversed`.
Absolute allocation levels vary with JVM optimization, as in earlier runs;
do not multiply improvements measured in independent campaigns.

The final same-revision mode comparison (`streaming-final-acceptance`, again
1 JVM per mode, 2 warmups, 10 measurements) observed:

| Mode | Wall ms | CPU ms | Allocated bytes |
| --- | ---: | ---: | ---: |
| Default tree | 228.509 | 227.112 | 266,537,224 |
| Final streaming | 192.946 | 192.375 | 180,059,304 |

This is 86,477,920 fewer allocated bytes per save (-32.44%), -15.56% wall and
-15.29% CPU in this campaign. The ordinary JSON SHA-256 still matches the
pre-streaming revision; streaming JSON-tree equality and reload validation pass.
These codec results do not include PostgreSQL or complete document saves.

A preliminary cache of immutable class-name/boolean primitives was not retained.
Its observed allocation change ranged from -0.025% to -0.856%, affected by
the already observed JVM allocation regimes. Streaming removes class-name
wrappers entirely, without an additional primitive cache.

### Non-streaming String boundaries

Sirius Web persists JSON as a `String`: saving currently grows a default
`ByteArrayOutputStream` before UTF-8 decoding, while loading encodes that
`String` back to UTF-8 bytes. One fresh JVM per operation, two warmups and ten
measurements gave these same-revision observations:

| Operation | Wall ms | CPU ms | Allocated bytes |
| --- | ---: | ---: | ---: |
| Save to String | 247.310 | 242.755 | 367,920,448 |
| Save to exactly sized byte buffer, then String | 238.743 | 236.486 | 323,950,272 |
| Save directly to characters | 368.331 | 362.015 | 375,572,352 |
| Load from String through UTF-8 bytes | 475.512 | 403.602 | 499,585,904 |
| Load directly from characters | 451.065 | 388.912 | 393,485,060 |

Exact byte-buffer sizing removed 43.97 MB, or 11.95% of save allocations. It
requires a reliable caller-owned capacity hint; `Resource.save(OutputStream)`
cannot resize an already supplied stream. Direct character output was rejected:
it allocated 2.08% more and took about 49% longer than the current String path.

Direct character input removed 106.10 MB, or 21.24% of load allocations, by
avoiding `String.getBytes(UTF_8)`. This evidence led to `loadFromString`, which
preserves the EMF resource lifecycle and all JSON callbacks. When the existing
`ResourceHandler` contract needs an `InputStream`, the method automatically
uses the byte path and preserves its `preLoad`/`postLoad` behavior.
All variants preserved the model/ID fingerprint and the save variants produced
the same 25,347,610 UTF-8 bytes. These are single-JVM observations without a
between-JVM confidence interval. Artifacts:
`target/performance/character-boundary-acceptance`.

Two cross-ordered comparisons of the final API used one fresh JVM per path, two
warmups and ten measurements. They reduced allocations by 102.77–102.84 MB per
load, or 20.59–20.71%. Wall and CPU results changed direction with run order,
so no timing gain is claimed. Artifacts:
`target/performance/string-load-api-comparison` and its `-reversed` run.

Reusing one immutable class-name `JsonPrimitive` per `EClass` was also rejected.
A 2+5 screening saved 2.21 MB per save (0.83%) but increased median wall and CPU
time by about 4%; the extra identity-map lookup was not justified.

### Optimized JSON versus EMF binary serialization

The diagnostic uses `XMIResourceImpl` with `XMLResource.OPTION_BINARY` and the
same Apollo model, persistent-state fingerprint and extrinsic IDs. Preparation,
disk access and validation remain outside timing. Both serializers allocate a
fresh `ByteArrayOutputStream`, save the resource and call `toByteArray` inside
the measured boundary. JSON and binary run in separate JVMs with one JVM per
format, two warmups and ten measurements. The resource URI is
`sirius:///apollo-11` for both formats.

| Order | Format | Wall ms | CPU ms | Allocated bytes | Output bytes |
| --- | --- | ---: | ---: | ---: | ---: |
| Binary then JSON | Optimized JSON | 226.345 | 225.220 | 264,320,016 | 25,347,610 |
| Binary then JSON | EMF binary | 287.972 | 287.951 | 103,717,488 | 13,248,226 |
| JSON then binary | Optimized JSON | 222.563 | 220.286 | 264,320,016 | 25,347,610 |
| JSON then binary | EMF binary | 278.641 | 278.635 | 103,717,488 | 13,248,226 |

In these two cross-ordered comparisons, binary saving was 25.20–27.23% slower
in wall time and 26.49–27.85% slower in CPU than the optimized JSON serializer.
It allocated 60.76% fewer bytes and produced a 47.73% smaller representation.
The two codecs therefore remain in the same time order of magnitude on Apollo;
binary is an allocation and size reference, not a faster-time lower bound.

The binary loader reproduced all checked attributes, ordered references,
containment, unsettable state and IDs without loading an external resource.
This semantic fingerprint does not establish compatibility with JSON-specific
processors, object handlers, serialization listeners, migrations or PostgreSQL
content. The figures are separate single-JVM observations without a
between-JVM confidence interval.

## Compatibility ledger

The following are source-level reasons for retaining each candidate, separate
from the performance measurements needed to accept its claimed benefit.

| Candidate | Preserved behavior and proof boundary | Regression coverage |
| --- | --- | --- |
| Buffered JSON character output | `JsonResourceImpl.doSave` buffers only without a resource handler. With a handler, `postSave` still sees the original unflushed stream boundary. Encoding, indentation, HTML escaping and writer closure stay unchanged. | `JsonWriterTests`: exact Unicode bytes in UTF-8/UTF-16LE, indentation, handler-visible byte count and final-flush failure propagation |
| Unsynchronized private writer | The writer is private, created and consumed within one `doSave` call. It keeps the same 8 KiB capacity and delegates encoding, flushing and closing to `OutputStreamWriter`. A try-with-resources closes the delegate even if the final buffered write fails. The resource-handler path is unchanged. | The same `JsonWriterTests` plus the complete 528-test verification; the Apollo harness checks byte identity and round-trip structure |
| Pre-sized reference arrays | Only the initial backing-list capacity changes. Known `List`, `Collection` and `InternalEList` sizes are read once; element order, iteration, serialization and callbacks are unchanged. Unknown iterable sizes retain zero initial capacity. | Apollo byte identity and round-trip checks in both acceptance orders; complete Maven verification |
| Repeated ID assignment | `JsonResourceImpl.setID` still calls `IDManager.setId` and puts the new mapping on every assignment. It only avoids removing a key immediately before replacing the same key. A changed ID still removes its previous entry. | `RepeatedIDTests`: callback counts, equal but distinct ID strings, reassignment and detach |
| Frozen feature serialization plan | `GsonEObjectSerializer.serializeEAllStructuralFeatures` caches only metadata declared by exact frozen `EClassImpl` classes within that serializer. Other implementations retain iterator traversal and generic dispatch. Custom ordering retains the full ordered feature list. Each iteration rechecks transient/derived options and the feature filter, including after child callbacks; filters can still force excluded features. Mutable inherited and custom features remain observable. | `FrozenFeaturesSerializationTests`: exact output, explicit options, filter/comparator calls, custom-list iteration, mutable metadata, custom feature access, mutable external superclasses and child callbacks changing remaining parent options |

Frozen metadata is subject to EMF's contract that a frozen model must not be
modified; freezing does not make all Java setters physically immutable.
Mutable metamodels use the uncached path. The feature cache neither stores
EObjects nor introduces a process-wide metamodel cache.

### Rejected load shortcuts

The containment trial restricted `addUnique` to exact standard containment
list classes and checked the actual owner through non-resolving
`eInternalContainer()`. Its tests covered order, opposites, callback
preinsertion, redirected owners, specialized lists and proxy containers.
The split trial guarded both existing `split(" ")` calls with an ASCII-space
check, preserving the original split logic and URI processor ordering. Tests
covered both cardinalities, local and typed external IDs and unusual whitespace.
The latest complete Maven verification passed these tests before removal.

These compatibility checks did not establish a performance benefit. The
five-fork results above led to restoring `GsonEObjectDeserializer` exactly
to R0 and removing the candidate-only test classes from the production tree.
Source and test copies remain under `target/performance/rejected-load/`,
with their original source-directory hierarchy, for reproducing the trials.
These ignored artifacts must be archived separately before cleaning `target`.

JDK 21 source constructs a one-element array for delimiter-free `String.split`.
That is an optimization hypothesis, not proof that the array survives JIT
optimization in this workload. Escape analysis may eliminate such allocation;
the campaign does not prove that explanation, only the absence of a measured
allocation improvement for the proposed guard.

### Rejected reference-classification cache

`docKindMany` scans a list to decide whether all entries use local fragments
or cross-document URI syntax. Repeating it for every element is quadratic
for local lists: a synthetic local list produces 400 target visits at size 20
and 40,000 at size 200. However, classification is observable between elements:
`JsonHelper.getIDREF` invokes `Resource.getURIFragment`, which may be overridden
and may reach a custom ID manager. These extension callbacks can move a later
target to another resource. A cache guarded only against custom `JsonHelper`
instances still changes the resulting JSON.

This worst case does not occur in Apollo. The accepted Sirius Web input has
738 serialized many-valued non-containment lists (`Dependency.client` and
`Dependency.supplier`, 369 each), all local and all of cardinality one. The
current implementation therefore performs 738 classifications and 738 target
visits; caching one classification per list removes no scan. In the scoped
accepted-candidate JFR, `docKindMany` has no CPU sample and accounts for
1,571,072 of 8,083,929,160 sampled allocation bytes (0.0194%, inclusive). The
whole many-reference serializer accounts for 0.0453% inclusive. These sampled
figures are estimates, but they rule this path out as a material Apollo hotspot.

`SerializeManyReferencesTests.resourceFragmentCallbackCanChangeReferenceClassification`
encodes this case with the default helper: the first target's fragment callback
moves the second target, which must then use an external URI. The per-element
classification was restored. Other tests retain coverage of custom helpers,
mixed references, URI listeners, unresolved proxies and local ordering.
The earlier linear-complexity assertion was removed because it prescribed the
rejected implementation. Classifying targets independently, always writing
HREFs, or assuming stable ownership would alter JSON or callback/listener
semantics. A new explicit stable-ownership contract could enable a linear fast
path for another workload, but is not justified by the Sirius Web target data.
No retained runtime gain is attributed to this work.

### Tree and callback constraints

Both codec adapters implement Gson tree-based interfaces. On load,
`GsonEObjectDeserializer.deserialize` passes the complete root `JsonObject`
to `IJsonResourceProcessor.preDeserialization` before loading objects. On save,
object handlers and serialization listeners receive complete object subtrees.
`IJsonResourceProcessor.postSerialization` receives the mutable root after the
`json` and `ns` members are attached, but before `schemaLocation` and `content`
are added; processors can inspect or add root members at that point. A streaming
replacement cannot discard these trees while preserving this contract and
callback ordering.

This rules out a blanket replacement of the current codec with a tree-free
streaming implementation. The optional path measured above has an explicit
compatibility boundary: it skips object-tree callbacks, retains header
processors, and rejects unsupported helper options. Its codec benefits do not
establish compatibility with arbitrary Sirius Web migration participants;
application adoption and integration measurements remain separate work.

Likewise, generic suppression of `IDManager.getOrCreateId`, `setId`, `findId`
or `clearId` calls is not equivalent to caching manager selection. Managers
can maintain adapters, indexes or other state. UUID allocation seen inside a
particular manager cannot justify skipping these public extension callbacks
for all managers. Changes to Sirius Web's manager would be a separate
application contribution with its own measurements and lifecycle tests.

## Attribution still required

The final CPU/allocation breakdown must distinguish codec work from work
called by the codec: reflective feature selection, ID/URI computation,
containment/reference insertion, deferred-reference resolution, Gson tree
construction/parsing, and character encoding/output. Report inclusive cost
to locate expensive call paths and exclusive cost to identify implementations
to change; overlapping inclusive percentages are not additive.

JFR allocation samples locate allocation sources but do not replace measured
bytes per operation. Temporary allocation, GC activity and memory retained
after loading answer different questions and must not be presented as the
same metric. Any category unsupported by the collected profiles remains
unquantified, rather than receiving an inferred percentage from its apparent
source-code complexity.

## Acceptance

For new acceptance comparisons, use one fresh JVM per variant, two warmups
and ten measured iterations (`FORKS=1 WARMUP=2 ITERATIONS=10`). Keep all
functional checks. Exploratory screening may use five measurements with the
same one JVM and two warmups; it does not replace the ten-measurement run.

This acceptance protocol deliberately limits execution cost and statistical
confidence. Two warmups may not stabilize the JVM, and one JVM per variant
does not quantify between-JVM variability. Report single-JVM observations;
do not treat the ten iterations as independent JVM replicas or derive a
cross-JVM confidence interval from them. Record each campaign's protocol and
keep its results separate; historical tables retain their recorded protocols.

Compare exact JSON bytes against the reference, validate persisted model
structure and IDs after loading, run existing tests and targeted regressions.
Check each retained optimization against the reference using this protocol
and state the limits of its single-JVM evidence.
Do not interpret a single profile or a noisy elapsed-time difference as a gain.
Report codec improvements separately from application and PostgreSQL costs.

Each optimization commit records its observed elapsed-time and allocation
changes, the corpus, comparison boundary and measurement protocol. Mark
single-fork screening results as provisional and inconclusive timing changes
as such. Infrastructure and regression-only commits claim no runtime gain.

This investigation cannot guarantee a mathematical global optimum or a
tenfold improvement. Its defensible result is a reproducible set of measured
changes, source-backed compatibility decisions and an explicit account of
remaining costs and untested alternatives. Apollo's merged resource does not
establish behavior or performance for all document fragmentation patterns,
model depths, reference cardinalities, migrations or custom metamodels.
Codec timings also do not establish PostgreSQL or end-to-end user latency.
Those limits remain even if the corrected campaign reports a large gain.
