#!/usr/bin/env bash
set -e

PROGRAM="${1:-program}"

docker run --rm \
  --platform linux/amd64 \
  -v "$PWD":/work \
  -w /work \
  ubuntu:latest \
  bash -c "./$PROGRAM"
