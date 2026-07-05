#!/usr/bin/env bash
set -e

PROGRAM=""
METRICS=false
RUNS=1
IMAGE="kavah-benchmark:latest"

CPU_LIMIT="1"
CPU_SET="0"
MEMORY_LIMIT="2g"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --metrics) METRICS=true; shift ;;
    --runs) RUNS="$2"; shift 2 ;;
    --cpus) CPU_LIMIT="$2"; shift 2 ;;
    --cpu-set) CPU_SET="$2"; shift 2 ;;
    --memory) MEMORY_LIMIT="$2"; shift 2 ;;
    --image) IMAGE="$2"; shift 2 ;;
    *) PROGRAM="$1"; shift ;;
  esac
done

if [[ -z "$PROGRAM" ]]; then
  echo "Usage: $0 <executable> [--metrics] [--runs N] [--cpus N] [--cpu-set N] [--memory SIZE] [--image IMAGE]"
  exit 1
fi

if [[ ! -f "$PROGRAM" ]]; then
  echo "Error: executable '$PROGRAM' not found"
  exit 1
fi

echo "Benchmark environment:"
echo "Image: $IMAGE"
echo "CPU limit: $CPU_LIMIT"
echo "CPU set: $CPU_SET"
echo "Memory limit: $MEMORY_LIMIT"
echo

DOCKER_ARGS=(
  --rm
  --platform linux/amd64
  --cpus="$CPU_LIMIT"
  --cpuset-cpus="$CPU_SET"
  --memory="$MEMORY_LIMIT"
  --memory-swap="$MEMORY_LIMIT"
  -v "$PWD":/work
  -w /work
  "$IMAGE"
)

if [[ "$METRICS" == true ]]; then
  docker run "${DOCKER_ARGS[@]}" bash -lc "
    set -e

    user_sum=0
    sys_sum=0
    elapsed_sum=0
    mem_sum=0
    successful_runs=0

    for i in \$(seq 1 $RUNS); do
      echo \"========== Run \$i ==========\"

      set +e
      /usr/bin/time -f 'METRICS %U %S %e %M' ./$PROGRAM 2> time.out
      status=\$?
      set -e

      cat time.out
      echo

      if [[ \$status -ne 0 ]]; then
        echo \"Run \$i failed with status \$status. Skipping averages.\"
        exit \$status
      fi

      metrics_line=\$(grep '^METRICS ' time.out)
      user=\$(echo \"\$metrics_line\" | awk '{print \$2}')
      sys=\$(echo \"\$metrics_line\" | awk '{print \$3}')
      elapsed=\$(echo \"\$metrics_line\" | awk '{print \$4}')
      mem=\$(echo \"\$metrics_line\" | awk '{print \$5}')

      user_sum=\$(awk -v a=\"\$user_sum\" -v b=\"\$user\" 'BEGIN { print a + b }')
      sys_sum=\$(awk -v a=\"\$sys_sum\" -v b=\"\$sys\" 'BEGIN { print a + b }')
      elapsed_sum=\$(awk -v a=\"\$elapsed_sum\" -v b=\"\$elapsed\" 'BEGIN { print a + b }')
      mem_sum=\$((mem_sum + mem))
      successful_runs=\$((successful_runs + 1))

      echo
    done

    awk -v runs=\"\$successful_runs\" \
        -v user=\"\$user_sum\" \
        -v sys=\"\$sys_sum\" \
        -v elapsed=\"\$elapsed_sum\" \
        -v mem=\"\$mem_sum\" \
        'BEGIN {
          printf \"========== Averages over %d runs ==========\n\", runs
          printf \"Average user time: %.6f s\n\", user / runs
          printf \"Average system time: %.6f s\n\", sys / runs
          printf \"Average elapsed time: %.6f s\n\", elapsed / runs
          printf \"Average max memory: %.2f KB\n\", mem / runs
        }'

    rm -f time.out
  "
else
  docker run "${DOCKER_ARGS[@]}" "./$PROGRAM"
fi
