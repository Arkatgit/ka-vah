#!/usr/bin/env bash
set -e

SOURCE=""
OUTPUT=""
OPTIMIZATION="optimized"
METRICS=false
BC=false
IMAGE="kavah-benchmark:latest"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --opt)
            case "$2" in
                optimized|none)
                    OPTIMIZATION="$2"
                    ;;
                *)
                    echo "Unknown optimization level: $2"
                    echo "Valid values: optimized, none"
                    exit 1
                    ;;
            esac
            shift 2
            ;;
        --metrics)
            METRICS=true
            shift
            ;;
        --bc)
            BC=true
            shift
            ;;
        *)
            if [[ -z "$SOURCE" ]]; then
                SOURCE="$1"
            elif [[ -z "$OUTPUT" ]]; then
                OUTPUT="$1"
            else
                echo "Unexpected argument: $1"
                exit 1
            fi
            shift
            ;;
    esac
done

if [[ -z "$SOURCE" || -z "$OUTPUT" ]]; then
    echo "Usage: $0 <source.kv> <output> [--opt optimized|none] [--metrics] [--bc]"
    exit 1
fi

CMD=(
    java
    -cp "libs/jparsec-3.1.jar:out/production/ka-vah"
    Main
    --file "$SOURCE"
    --pipeline "$OPTIMIZATION"
    --exe
    --output "$OUTPUT"
)

if [[ "$BC" == true ]]; then
    CMD+=(--bc)
fi

if [[ "$METRICS" == true ]]; then
    docker run --rm \
        --platform linux/amd64 \
        -v "$PWD":/work \
        -w /work \
        "$IMAGE" \
        /usr/bin/time -v "${CMD[@]}"
else
    docker run --rm \
        --platform linux/amd64 \
        -v "$PWD":/work \
        -w /work \
        "$IMAGE" \
        "${CMD[@]}"
fi