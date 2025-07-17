#!/bin/bash

PROJECTS=(
sharp-dependencies
#sharp-common
sharp-database
#sharp-excel
#sharp-meta
#sharp-fileupload
#sharp-formflow
#sharp-mail
#sharp-report
#sharp-generator
#sharp-sms
#sharp-notification
)

for p in ${PROJECTS[@]}
do
  mvn clean install -f $p
done
