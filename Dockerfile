# FROM ubuntu:latest
#
# RUN apt-get update && \
#     apt-get install -y time && \
#     rm -rf /var/lib/apt/lists/*

# FROM ubuntu:latest
#
# RUN apt-get update && \
#     apt-get install -y time ghc build-essential && \
#     rm -rf /var/lib/apt/lists/*
#
# FROM ubuntu:22.04
#
# ENV DEBIAN_FRONTEND=noninteractive
# ENV GHC_VERSION=9.2.8
# ENV PATH="/opt/ghc/bin:${PATH}"
#
# RUN apt-get update && apt-get install -y --no-install-recommends \
#     build-essential curl xz-utils \
#     libffi-dev libgmp-dev libncurses-dev libtinfo5 \
#     zlib1g-dev ca-certificates \
#     && rm -rf /var/lib/apt/lists/*
#
# RUN curl -L https://downloads.haskell.org/ghc/${GHC_VERSION}/ghc-${GHC_VERSION}-x86_64-ubuntu20.04-linux.tar.xz \
#     | tar -xJ -C /tmp && \
#     cd /tmp/ghc-${GHC_VERSION} && \
#     ./configure --prefix=/opt/ghc && \
#     make install && \
#     rm -rf /tmp/ghc-${GHC_VERSION}
#
# WORKDIR /work
# CMD ["/bin/bash"]

FROM --platform=linux/amd64 haskell:9.2.8-slim AS ghc-source

FROM --platform=linux/amd64 ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV PATH="/opt/ghc/9.2.8/bin:${PATH}"

# RUN apt-get update && apt-get install -y --no-install-recommends \
#     build-essential curl git \
#     libffi-dev libgmp-dev libncurses-dev libtinfo5 \
#     pkg-config zlib1g-dev ca-certificates \
#     && rm -rf /var/lib/apt/lists/*


RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    git \
    time \
    procps \
    coreutils \
    libffi-dev \
    libgmp-dev \
    libncurses-dev \
    libtinfo5 \
    pkg-config \
    zlib1g-dev \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*




COPY --from=ghc-source /opt/ghc/9.2.8 /opt/ghc/9.2.8

RUN ghc --version

WORKDIR /work
CMD ["/bin/bash"]


