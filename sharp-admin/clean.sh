#!/bin/bash

directory_name="sharp-admin"

cd /usr/local/projects/$directory_name
PID=`ps -ef | pgrep -f "8082"`
if [  -n "$PID" ]
then
        kill -9 $PID
fi
rm -f $directory_name-1.0-SNAPSHOT.jar
cp deploy/$directory_name-1.0-SNAPSHOT.jar .
nohup /usr/local/jdk1.8.0_65/bin/java -Xms1024m -Xmx2048m -Dspring.profiles.active=prod -jar $directory_name-1.0-SNAPSHOT.jar --server.port=8082 &
exit