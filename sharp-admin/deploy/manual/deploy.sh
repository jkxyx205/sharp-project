#!/bin/bash
source ./config.sh

cd $dir/$project_name
PID=$(ps -ef | pgrep -f "$port")

if [ -n "$PID" ]; then
    kill -9 $PID
fi
rm -f $project_name-3.0-SNAPSHOT.jar
cp deploy/$project_name-3.0-SNAPSHOT.jar .
nohup java -Xms1024m -Xmx2048m -Dspring.profiles.active=prod -jar $project_name-3.0-SNAPSHOT.jar --server.port=$port &
exit
