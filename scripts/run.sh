#!/bin/bash
set -e

cd "$(git rev-parse --show-toplevel)"

./init-local-env.sh

java -jar -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 target/app.jar
