#!/bin/bash
set -eu -o pipefail

changed=0

echo "Setting up local development environment.."

if ! [ -f overrides.properties ]; then
  echo
  echo "Generating overrides.properties"

  (sed 's/^ \+//g' <<TEMPLATE
server.port=8080
build.timestamp=2007-12-03T10:15:30.00Z
build.commit=5d974d0
build.branch=master
build.number=0

orderQueue.enabled=false
aws.localstack.enabled=true
TEMPLATE
  )>>overrides.properties
  changed=1
else
  echo "overrides.properties is already set up - check the history of init-env.sh to see if it is out of date"
fi

if [ $changed -eq 0 ]; then
  echo "No changes performed"
fi
