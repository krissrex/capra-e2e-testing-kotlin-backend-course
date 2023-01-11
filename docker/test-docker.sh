#!/bin/bash

JAR_NAME="app"
SERVICE_NAME="<Service>"
SERVICE_NAMESPACE="<customer-or-service-namespace>"
GIT_COMMIT_HASH="$(git rev-parse HEAD)"

set -e
cp -p "../target/$JAR_NAME.jar" app.jar

docker build \
  --platform linux/amd64 \
  --build-arg service_name=$SERVICE_NAME \
  --build-arg service_namespace=$SERVICE_NAMESPACE \
  --build-arg service_version=$GIT_COMMIT_HASH \
  -t $JAR_NAME .

docker run -it --rm -p 8080:8080 $JAR_NAME
