#!/bin/bash
cd /usr/local/projects/dao
PID=`ps -ef | pgrep -f "8082"`
if [  -n "$PID" ]
then
        kill -9 $PID
fi
rm -f sharp-admin-1.0-SNAPSHOT.jar
cp deploy/sharp-admin-1.0-SNAPSHOT.jar .
nohup /usr/local/jdk1.8.0_65/bin/java -Xms1024m -Xmx2048m -Dspring.profiles.active=prod -jar sharp-admin-1.0-SNAPSHOT.jar --server.port=8082 &
exit