#!/usr/bin/env bash
# Copyright (c) 2026 Obeo. SPDX-License-Identifier: EPL-2.0
set -euo pipefail
if (( $# < 3 || $# > 5 )); then
    echo 'Usage: bash benchmarks/run.sh corpus.bin dependency-classpath.txt output-directory [baseline-classes] [candidate-classes]' >&2
    exit 2
fi
benchmark_root=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.." && pwd)
benchmark_java=${BENCHMARK_JAVA_HOME:-/home/cedric/bin/java/temurin_21/jdk-21.0.6+7}
benchmark_corpus=$(realpath -- "$1")
benchmark_dependencies=$(<"$2")
IFS=: read -r -a benchmark_entries <<< "$benchmark_dependencies"
benchmark_dependencies=
for entry in "${benchmark_entries[@]}"; do
    benchmark_dependencies+="$(realpath -- "$entry"):"
done
benchmark_dependencies=${benchmark_dependencies%:}
benchmark_output=$(realpath -m -- "$3")
benchmark_baseline=$(realpath -- "${4:-$benchmark_root/org.eclipse.sirius.emfjson/target/classes}")
benchmark_candidate=${5:-}
test -f "$benchmark_baseline/org/eclipse/sirius/emfjson/resource/JsonResourceImpl.class"
if [[ -n "$benchmark_candidate" ]]; then
    test -f "$benchmark_candidate/org/eclipse/sirius/emfjson/resource/JsonResourceImpl.class"
fi
benchmark_forks=${FORKS:-5}
benchmark_warmup=${WARMUP:-10}
benchmark_iterations=${ITERATIONS:-30}
benchmark_heap=${HEAP:-2g}
mkdir -p -- "$benchmark_output/classes"
"${MAVEN:-mvn}" -q -f "$benchmark_root/org.eclipse.sirius.emfjson/pom.xml" dependency:build-classpath -DincludeScope=test -Dmdep.outputFile="$benchmark_output/emfjson-classpath.txt"
benchmark_dependencies="$(<"$benchmark_output/emfjson-classpath.txt"):$benchmark_dependencies"
IFS=: read -r -a benchmark_entries <<< "$benchmark_dependencies"
if [[ -f "$benchmark_output/baseline.json" ]] && ! cmp -s <(sha256sum "$benchmark_corpus") "$benchmark_output/corpus.sha256"; then
    echo 'Existing baseline corpus differs; use a fresh output directory.' >&2
    exit 2
fi
"$benchmark_java/bin/javac" --release 21 -cp "$benchmark_baseline:$benchmark_dependencies" -d "$benchmark_output/classes" "$benchmark_root"/benchmarks/*.java
"$benchmark_java/bin/java" -version 2> "$benchmark_output/java-version.txt"
sha256sum "$benchmark_corpus" > "$benchmark_output/corpus.sha256"
{
    echo "forks=$benchmark_forks warmup=$benchmark_warmup iterations=$benchmark_iterations heap=$benchmark_heap"
    echo "baseline=$benchmark_baseline candidate=$benchmark_candidate"
    for entry in "${benchmark_entries[@]}"; do
        if [[ -f "$entry" ]]; then sha256sum "$(realpath -- "$entry")"; fi
    done
} > "$benchmark_output/configuration.txt"
benchmark_names=(baseline)
benchmark_versions=("$benchmark_baseline")
if [[ -n "$benchmark_candidate" ]]; then
    benchmark_names+=(candidate)
    benchmark_versions+=("$(realpath -- "$benchmark_candidate")")
fi
for index in "${!benchmark_names[@]}"; do
    variant=${benchmark_names[index]}
    benchmark_cp="${benchmark_versions[index]}:$benchmark_output/classes:$benchmark_dependencies"
    if [[ "$variant" == baseline && ! -f "$benchmark_output/baseline.json" ]]; then
        "$benchmark_java/bin/java" -Xms"$benchmark_heap" -Xmx"$benchmark_heap" -cp "$benchmark_cp" SysMLBenchmark prepare "$benchmark_corpus" "$benchmark_output" > "$benchmark_output/prepare.log" 2>&1
    fi
    for operation in save load; do
        for ((fork=1; fork<=benchmark_forks; fork++)); do
            echo "$variant $operation fork $fork/$benchmark_forks" >&2
            "$benchmark_java/bin/java" -Xms"$benchmark_heap" -Xmx"$benchmark_heap" -cp "$benchmark_cp" SysMLBenchmark "$operation" "$benchmark_corpus" "$benchmark_output" "$benchmark_warmup" "$benchmark_iterations" > "$benchmark_output/$variant-$operation-fork$fork.csv" 2> "$benchmark_output/$variant-$operation-fork$fork.log"
        done
        if [[ ${PROFILE:-0} == 1 ]]; then
            "$benchmark_java/bin/java" -Xms"$benchmark_heap" -Xmx"$benchmark_heap" -cp "$benchmark_cp" SysMLBenchmark "$operation" "$benchmark_corpus" "$benchmark_output" "$benchmark_warmup" "$benchmark_iterations" "$benchmark_output/profile-$variant-$operation.jfr" > "$benchmark_output/profile-$variant-$operation.csv" 2> "$benchmark_output/profile-$variant-$operation.log"
        fi
    done
done
