#!/bin/bash
source ./config.sh

docker-compose stop $project_name
docker-compose rm $project_name -f
docker rmi $project_name:2.0
docker build -t $project_name:2.0 .
docker-compose create $project_name
docker-compose start $project_name