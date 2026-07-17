#!/usr/bin/env bash

set -e

EXEC="$1"
shift

RUNS=10

while [[ $# -gt 0 ]]; do
    case "$1" in
        --runs)
            RUNS="$2"
            shift 2
            ;;
        *)
            shift
            ;;
    esac
done

if [[ ! -x "$EXEC" ]]; then
    echo "Error: executable not found or not executable: $EXEC"
    exit 1
fi

user_sum=0
system_sum=0
elapsed_sum=0
mem_sum=0

echo "Executable: $EXEC"
echo "Runs: $RUNS"
echo

for ((i=1; i<=RUNS; i++)); do
    echo "========== Run $i =========="

    out=$(
         { /usr/bin/time -lp "$EXEC"; } 2>&1
    )

    echo "$out"
    echo

    user=$(echo "$out" | awk '/^user / {print $2}')
    system=$(echo "$out" | awk '/^sys / {print $2}')
    elapsed=$(echo "$out" | awk '/^real / {print $2}')
    mem_bytes=$(echo "$out" | awk '/maximum resident set size/ {print $1}')

    user_sum=$(awk -v a="$user_sum" -v b="$user" 'BEGIN { print a + b }')
    system_sum=$(awk -v a="$system_sum" -v b="$system" 'BEGIN { print a + b }')
    elapsed_sum=$(awk -v a="$elapsed_sum" -v b="$elapsed" 'BEGIN { print a + b }')
    mem_sum=$(awk -v a="$mem_sum" -v b="$mem_bytes" 'BEGIN { print a + b }')
done

echo "========== Averages =========="

awk \
    -v u="$user_sum" \
    -v s="$system_sum" \
    -v e="$elapsed_sum" \
    -v m="$mem_sum" \
    -v n="$RUNS" '
BEGIN {
    printf "Average user time:    %.6f s\n", u / n
    printf "Average system time:  %.6f s\n", s / n
    printf "Average elapsed time: %.6f s\n", e / n
    printf "Average max memory:   %.2f KB\n", (m / n) / 1024
}'


##!/usr/bin/env bash
#
#EXEC="$1"
#shift
#
#RUNS=10
#
#while [[ $# -gt 0 ]]; do
#    case "$1" in
#        --runs)
#            RUNS="$2"
#            shift 2
#            ;;
#        *)
#            shift
#            ;;
#    esac
#done
#
#to_seconds() {
#    echo "$1" | awk '
#    {
#        gsub("m", " ")
#        gsub("s", "")
#        print ($1 * 60) + $2
#    }'
#}
#
#user_sum=0
#system_sum=0
#total_sum=0
#
#echo "Executable: $EXEC"
#echo "Runs: $RUNS"
#echo
#
#for ((i=1; i<=RUNS; i++)); do
#    echo "========== Run $i =========="
#
#    result=$(
#        { time "$EXEC" >/dev/null; } 2>&1
#    )
#
#    echo "$result"
#
#    real_raw=$(echo "$result" | awk '/^real/ {print $2}')
#    user_raw=$(echo "$result" | awk '/^user/ {print $2}')
#    sys_raw=$(echo "$result" | awk '/^sys/ {print $2}')
#
#    real=$(to_seconds "$real_raw")
#    user=$(to_seconds "$user_raw")
#    system=$(to_seconds "$sys_raw")
#
#    user_sum=$(awk -v a="$user_sum" -v b="$user" 'BEGIN{print a+b}')
#    system_sum=$(awk -v a="$system_sum" -v b="$system" 'BEGIN{print a+b}')
#    total_sum=$(awk -v a="$total_sum" -v b="$real" 'BEGIN{print a+b}')
#
#    echo
#done
#
#echo "========== Averages =========="
#
#awk -v u="$user_sum" -v s="$system_sum" -v t="$total_sum" -v n="$RUNS" '
#BEGIN {
#    printf "Average user time:   %.6f s\n", u/n
#    printf "Average system time: %.6f s\n", s/n
#    printf "Average total time:  %.6f s\n", t/n
#}'