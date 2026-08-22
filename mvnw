#!/bin/sh
# Maven Wrapper — downloads Apache Maven if not cached, then delegates.
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_PROPERTIES="$SCRIPT_DIR/.mvn/wrapper/maven-wrapper.properties"

DISTRIBUTION_URL="$(grep distributionUrl "$WRAPPER_PROPERTIES" | cut -d= -f2- | tr -d '\r')"
DISTRIBUTION_FILENAME="$(basename "$DISTRIBUTION_URL")"
DISTRIBUTION_NAME="${DISTRIBUTION_FILENAME%.zip}"

CACHE_DIR="${MAVEN_USER_HOME:-$HOME/.m2}/wrapper/dists/$DISTRIBUTION_NAME"

if [ ! -f "$CACHE_DIR/bin/mvn" ]; then
  echo "[mvnw] Downloading $DISTRIBUTION_URL" >&2
  mkdir -p "$CACHE_DIR"
  TMP="$(dirname "$CACHE_DIR")/$DISTRIBUTION_FILENAME"
  if command -v curl > /dev/null 2>&1; then
    curl -fsSL -o "$TMP" "$DISTRIBUTION_URL"
  elif command -v wget > /dev/null 2>&1; then
    wget -q -O "$TMP" "$DISTRIBUTION_URL"
  else
    echo "[mvnw] curl or wget required" >&2; exit 1
  fi
  unzip -q "$TMP" -d "$(dirname "$CACHE_DIR")"
  rm -f "$TMP"
fi

exec "$CACHE_DIR/bin/mvn" "$@"
