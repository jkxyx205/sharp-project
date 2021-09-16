#!/bin/bash

PROJECTS=(
#sharp-dependencies
#sharp-common
#sharp-database
#sharp-meta
#sharp-excel
#sharp-sms
sharp-security
)

for p in $PROJECTS
do
  mvn clean install -f $p
done
