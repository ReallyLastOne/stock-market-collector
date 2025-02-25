#!/bin/bash

echo "[INFO] Starting push-image.sh"

if [ -z "$DOCKER_LOGIN" ]; then
  echo "[ERROR] DOCKER_LOGIN is not set."
  exit 1
fi

if [ -n "$1" ]; then
  echo "[INFO] Decoding password from argument."
  DOCKER_PASSWORD=$(echo "$1" | base64 -d)
fi

if [ -z "$DOCKER_PASSWORD" ]; then
  echo "[ERROR] DOCKER_PASSWORD is not set."
  exit 1
fi

echo "[INFO] Logging into Docker..."
 docker login --username "$DOCKER_LOGIN" --password "$DOCKER_PASSWORD" docker.io

if [ $? -eq 0 ]; then
  echo "[SUCCESS] Logged in successfully."
else
  echo "[ERROR] Docker login failed."
  exit 1
fi

echo "[INFO] Pushing Docker image..."
docker push reallylastone/stock-market-collector

if [ $? -eq 0 ]; then
  echo "[SUCCESS] Docker image pushed successfully."
else
  echo "[ERROR] Failed to push Docker image."
  exit 1
fi
