#!/bin/bash

echo "[INFO] Starting build-image.sh"

SCRIPT_DIR=$(cd -- "$(dirname -- "$0")" && pwd)
echo "[INFO] Script directory: $SCRIPT_DIR"

PROJECT_DIR="$SCRIPT_DIR/.."
echo "[INFO] Project directory: $PROJECT_DIR"

echo "[INFO] Building Docker image..."
docker build -f "$SCRIPT_DIR/Dockerfile" -t reallylastone/stock-market-collector "$PROJECT_DIR"

if [ $? -eq 0 ]; then
  echo "[SUCCESS] Docker image built successfully."
else
  echo "[ERROR] Failed to build Docker image."
  exit 1
fi
