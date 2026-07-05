#!/usr/bin/env bash
set -e

ASM_FILE="${1:-test.asm}"
OUT_FILE="${2:-test}"

docker run --rm \
  --platform linux/amd64 \
  -v "$PWD":/work \
  -w /work \
  gcc:latest \
  bash -c "gcc -x assembler -nostdlib -no-pie -Wl,-e,_start '$ASM_FILE' -o '$OUT_FILE'"
