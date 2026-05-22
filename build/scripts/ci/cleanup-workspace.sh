#!/usr/bin/env bash
set -euo pipefail

if [ -z "${WORKSPACE:-}" ]; then
  echo "WORKSPACE unset or empty; nothing to clean."
  exit 0
fi
case "$WORKSPACE" in
  "/"|"")
    echo "Refusing to remove '$WORKSPACE'."
    exit 0
    ;;
esac
rm -rf -- "$WORKSPACE"
