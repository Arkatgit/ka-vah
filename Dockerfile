# FROM --platform=linux/amd64 haskell:9.2.8-slim AS ghc-source
#
# FROM --platform=linux/amd64 ubuntu:22.04
#
# ENV DEBIAN_FRONTEND=noninteractive
# ENV PATH="/opt/ghc/9.2.8/bin:${PATH}"
#
# # RUN apt-get update && apt-get install -y --no-install-recommends \
# #     build-essential curl git \
# #     libffi-dev libgmp-dev libncurses-dev libtinfo5 \
# #     pkg-config zlib1g-dev ca-certificates \
# #     && rm -rf /var/lib/apt/lists/*
#
#
# RUN apt-get update && apt-get install -y --no-install-recommends \
#     build-essential \
#     curl \
#     git \
#     time \
#     procps \
#     coreutils \
#     libffi-dev \
#     libgmp-dev \
#     libncurses-dev \
#     libtinfo5 \
#     pkg-config \
#     zlib1g-dev \
#     ca-certificates \
#     && rm -rf /var/lib/apt/lists/*
#
#
#
# COPY --from=ghc-source /opt/ghc/9.2.8 /opt/ghc/9.2.8
#
# RUN ghc --version
#
# WORKDIR /work
# CMD ["/bin/bash"]


# FROM --platform=linux/amd64 haskell:9.2.8-slim
FROM --platform=linux/amd64 haskell:9.14.1

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    time \
    procps \
    coreutils \
    libffi-dev \
    libgmp-dev \
    libncurses-dev \
    pkg-config \
    zlib1g-dev \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

RUN ghc --version

WORKDIR /work

CMD ["/bin/bash"]


