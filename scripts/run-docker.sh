#!/bin/bash
set -e

DOCKER_IMAGE_NAME="my-bartender-service:latest-local"

docker run -it --rm -p 8080:8080 $DOCKER_IMAGE_NAME
