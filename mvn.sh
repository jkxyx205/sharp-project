#!/bin/bash

PROJECTS=(
sharp-dependencies
sharp-common
sharp-database
#sharp-meta
#sharp-excel
#sharp-fileupload
#sharp-formflow
#sharp-mail
#sharp-report
#sharp-sms
)

for p in ${PROJECTS[@]}
do
  mvn clean install -f $p
done
