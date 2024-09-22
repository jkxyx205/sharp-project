#!/bin/bash

mvn clean package -Dmaven.test.skip=true
scp /Users/rick/Space/Workspace/sharp-project/sharp-admin/target/sharp-admin-1.0-SNAPSHOT.jar root@106.153.132.172:/usr/local/projects/sharp-admin/deploy

#ssh dao
# cd /usr/local/projects/sharp-admin
# sh clean.sh
#ps -ef | pgrep -f "8082"

ssh dao "bash -s < /usr/local/projects/sharp-admin/clean.sh"