#!/usr/bin/env bash
set -e

SOURCE=""
OUTPUT=""
IMAGE="kavah-benchmark:latest"
OPT_FLAGS="-O2"
METRICS=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --opt)
            case "$2" in
                optimized)
                    OPT_FLAGS="-O2"
                    ;;
                none)
                    OPT_FLAGS="-O0"
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
    echo "Usage: $0 <source.hs> <output> [--opt optimized|none] [--metrics]"
    exit 1
fi

if [[ "$METRICS" == true ]]; then
    docker run --rm \
        --platform linux/amd64 \
        -v "$PWD":/work \
        -w /work \
        "$IMAGE" \
        bash -c "
            /usr/bin/time -v \
            ghc $OPT_FLAGS \"$SOURCE\" -o \"$OUTPUT\"
        "
else
    docker run --rm \
        --platform linux/amd64 \
        -v "$PWD":/work \
        -w /work \
        "$IMAGE" \
        ghc $OPT_FLAGS "$SOURCE" -o "$OUTPUT"
fi






##!/usr/bin/env bash
#set -e
#
#SOURCE="$1"
#OUTPUT="$2"
#
#IMAGE="kavah-benchmark:latest"
#
#if [[ -z "$SOURCE" || -z "$OUTPUT" ]]; then
#    echo "Usage: $0 <source.hs> <output>"
#    exit 1
#fi
#
#docker run --rm \
#    --platform linux/amd64 \
#    -v "$PWD":/work \
#    -w /work \
#    "$IMAGE" \
#    ghc \
#        -O2 \
#        "$SOURCE" \
#        -o "$OUTPUT"


##!/usr/bin/env bash
#set -e
#
#SOURCE="$1"
#OUTPUT="$2"
#
#if [[ -z "$SOURCE" || -z "$OUTPUT" ]]; then
#  echo "Usage: $0 <source.hs> <output>"
#  exit 1
#fi
#
#docker run --rm \
#  --platform linux/amd64 \
#  -v "$PWD":/work \
#  -w /work \
#  kavah-benchmark \
#  ghc -O2 "$SOURCE" -o "$OUTPUT"