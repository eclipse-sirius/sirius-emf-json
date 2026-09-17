#!/usr/bin/env bash
# Copyright (c) 2026 Obeo. SPDX-License-Identifier: EPL-2.0
set -euo pipefail
if (( $# < 3 )); then
    echo 'Usage: bash benchmarks/run.sh corpus.bin dependency-classpath.txt output-directory [baseline-classes] [candidate-classes ...]' >&2
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
if [[ -e "$benchmark_output" ]]; then
    echo 'Output directory already exists; use a fresh output directory.' >&2
    exit 2
fi
benchmark_baseline=$(realpath -- "${4:-$benchmark_root/org.eclipse.sirius.emfjson/target/classes}")
test -f "$benchmark_baseline/org/eclipse/sirius/emfjson/resource/JsonResourceImpl.class"
benchmark_names=(baseline)
benchmark_versions=("$benchmark_baseline")
for entry in "${@:5}"; do
    test -f "$entry/org/eclipse/sirius/emfjson/resource/JsonResourceImpl.class"
    index=${#benchmark_names[@]}
    variant=candidate
    if (( index > 1 )); then variant=candidate$index; fi
    benchmark_names+=("$variant")
    benchmark_versions+=("$(realpath -- "$entry")")
done
benchmark_forks=${FORKS:-5}
benchmark_warmup=${WARMUP:-10}
benchmark_iterations=${ITERATIONS:-30}
benchmark_heap=${HEAP:-2g}
benchmark_profile=${PROFILE:-0}
benchmark_jvm=(-Xms"$benchmark_heap" -Xmx"$benchmark_heap" -XX:+UseG1GC)
read -r -a benchmark_operations <<< "${OPERATIONS:-save load}"
[[ $benchmark_forks =~ ^[0-9]+$ ]]
mkdir -p -- "$benchmark_output/classes"
"${MAVEN:-mvn}" -q -f "$benchmark_root/org.eclipse.sirius.emfjson/pom.xml" dependency:build-classpath -DincludeScope=test -Dmdep.outputFile="$benchmark_output/emfjson-classpath.txt"
benchmark_dependencies="$(<"$benchmark_output/emfjson-classpath.txt"):$benchmark_dependencies"
IFS=: read -r -a benchmark_entries <<< "$benchmark_dependencies"
"$benchmark_java/bin/javac" --release 21 -cp "$benchmark_baseline:$benchmark_dependencies" -d "$benchmark_output/classes" "$benchmark_root"/benchmarks/*.java
"$benchmark_java/bin/java" -version 2> "$benchmark_output/java-version.txt"
sha256sum "$benchmark_corpus" > "$benchmark_output/corpus.sha256"
{
    echo "forks=$benchmark_forks warmup=$benchmark_warmup iterations=$benchmark_iterations heap=$benchmark_heap"
    for index in "${!benchmark_names[@]}"; do
        echo "${benchmark_names[index]}=${benchmark_versions[index]}"
    done
    echo "operations=${benchmark_operations[*]}"
    echo "profile=$benchmark_profile"
    echo "nice=$(ps -o ni= -p "$$")"
    printf 'java-command='
    printf '%q ' "$benchmark_java/bin/java" "${benchmark_jvm[@]}"
    printf '\n'
    for entry in "${benchmark_entries[@]}"; do
        if [[ -f "$entry" ]]; then sha256sum "$(realpath -- "$entry")"; fi
    done
} > "$benchmark_output/configuration.txt"
hash_tree() {
    (cd -- "$1" && find . -type f -name "$2" -print0 | LC_ALL=C sort -z | xargs -0 -r sha256sum)
}
for index in "${!benchmark_names[@]}"; do
    hash_tree "${benchmark_versions[index]}" '*.class' > "$benchmark_output/${benchmark_names[index]}-classes.sha256"
done
hash_tree "$benchmark_root/benchmarks" '*.java' > "$benchmark_output/benchmark-sources.sha256"
hash_tree "$benchmark_output/classes" '*.class' > "$benchmark_output/benchmark-classes.sha256"
sha256sum "$benchmark_root/benchmarks/run.sh" "$benchmark_root/benchmarks/summarize.py" > "$benchmark_output/tools.sha256"
if [[ ! -f "$benchmark_output/baseline.json" ]]; then
    benchmark_cp="$benchmark_baseline:$benchmark_output/classes:$benchmark_dependencies"
    "$benchmark_java/bin/java" "${benchmark_jvm[@]}" -cp "$benchmark_cp" SysMLBenchmark prepare "$benchmark_corpus" "$benchmark_output" > "$benchmark_output/prepare.log" 2>&1
fi
for ((fork=1; fork<=benchmark_forks; fork++)); do
    for operation in "${benchmark_operations[@]}"; do
        for step in "${!benchmark_names[@]}"; do
            index=$step
            if (( fork % 2 == 0 )); then index=$((${#benchmark_names[@]} - 1 - step)); fi
            variant=${benchmark_names[index]}
            benchmark_cp="${benchmark_versions[index]}:$benchmark_output/classes:$benchmark_dependencies"
            echo "$variant $operation fork $fork/$benchmark_forks" >&2
            "$benchmark_java/bin/java" "${benchmark_jvm[@]}" -cp "$benchmark_cp" SysMLBenchmark "$operation" "$benchmark_corpus" "$benchmark_output" "$benchmark_warmup" "$benchmark_iterations" > "$benchmark_output/$variant-$operation-fork$fork.csv" 2> "$benchmark_output/$variant-$operation-fork$fork.log"
        done
    done
done
if [[ $benchmark_profile == 1 ]]; then
    for index in "${!benchmark_names[@]}"; do
        variant=${benchmark_names[index]}
        benchmark_cp="${benchmark_versions[index]}:$benchmark_output/classes:$benchmark_dependencies"
        for operation in "${benchmark_operations[@]}"; do
            "$benchmark_java/bin/java" "${benchmark_jvm[@]}" -cp "$benchmark_cp" SysMLBenchmark "$operation" "$benchmark_corpus" "$benchmark_output" "$benchmark_warmup" "$benchmark_iterations" "$benchmark_output/profile-$variant-$operation.jfr" > "$benchmark_output/profile-$variant-$operation.csv" 2> "$benchmark_output/profile-$variant-$operation.log"
        done
    done
fi
