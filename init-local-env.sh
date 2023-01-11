#!/bin/bash
set -eu -o pipefail

changed=0

echo "Setting up local development environment.."

if ! [ -f overrides.properties ]; then
  echo
  echo "Generating overrides.properties"

  (sed 's/^ \+//g' <<TEMPLATE
server.port=9000
build.timestamp=2007-12-03T10:15:30.00Z
build.commit=5d974d0
build.branch=master
build.number=0
cors.allow.origin=*
cors.allow.headers=origin,content-type,accept,authorization,x-request-id
cors.allow.methods=GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD
TEMPLATE
  )>>overrides.properties
  changed=1
else
  echo "overrides.properties is already set up - check the history of init-env.sh to see if it is out of date"
fi

if [ $changed -eq 0 ]; then
  echo "No changes performed"
fi