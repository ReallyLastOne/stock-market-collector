#bin/bash

docker login --username "${DOCKER_LOGIN}" --password "${DOCKER_PASSWORD}" docker.io

docker push reallylastone/stock-market-collector