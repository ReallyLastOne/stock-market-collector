#bin/bash

PROJECT_DIR="$dirname"

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
echo "script directory: $SCRIPT_DIR"

PROJECT_DIR=$SCRIPT_DIR/..
echo "project directory: $PROJECT_DIR"
echo "building docker image..."

docker build -f "$SCRIPT_DIR/Dockerfile" -t reallylastone/stock-market-collector "$PROJECT_DIR"
