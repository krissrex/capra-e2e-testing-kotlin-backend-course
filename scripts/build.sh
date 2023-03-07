#!/bin/bash
set -e

cd "$(git rev-parse --show-toplevel)"

# Build jar
GIT_COMMIT_HASH="$(git rev-parse HEAD)"
GIT_BRANCH_NAME="$(git rev-parse --abbrev-ref HEAD)"

mvn package -DskipTests -Dbuild.commit="$GIT_COMMIT_HASH" -Dbuild.branch="$GIT_BRANCH_NAME"

# Build Docker image
cd docker

JAR_NAME="app"
DOCKER_IMAGE_NAME="my-bartender-service:latest-local"

cp -p "../target/$JAR_NAME.jar" app.jar

docker build \
  --platform linux/amd64 \
  -t $DOCKER_IMAGE_NAME .
