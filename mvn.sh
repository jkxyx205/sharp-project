#!/bin/bash

PROJECTS=(
sharp-dependencies
sharp-common
sharp-database
sharp-database2
sharp-excel
sharp-meta
sharp-report
sharp-fileupload
sharp-formflow
sharp-mail
sharp-generator
sharp-sms
sharp-notification
)

for p in ${PROJECTS[@]}
do
  mvn clean install -f $p
done
