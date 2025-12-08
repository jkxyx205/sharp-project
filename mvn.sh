#!/bin/bash

PROJECTS=(
sharp-dependencies
sharp-common
sharp-database
sharp-meta
sharp-fileupload
sharp-formflow
sharp-mail
sharp-excel
sharp-report
sharp-generator
sharp-sms
)

for p in ${PROJECTS[@]}
do
  mvn clean install -f $p
done
