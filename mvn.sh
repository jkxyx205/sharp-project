#!/bin/bash

PROJECTS=(
sharp-dependencies
sharp-common
sharp-database
sharp-excel
sharp-sms
)

for p in $PROJECTS
do
  mvn clean install -f $p
done
