#!/bin/bash

# 获取当前工作目录的绝对路径
current_dir=$(pwd)
# 获取当前工作目录的名称
directory_name=$(basename "$current_dir")

mvn clean package -Dmaven.test.skip=true
scp $current_dir/target/$directory_name-1.0-SNAPSHOT.jar root@106.15.102.17:/usr/local/projects/$directory_name/deploy

#ssh dao
# cd /usr/local/projects/dao
# sh clean.sh
#ps -ef | pgrep -f "8082"

# 服务器免密登录 ssh dao
# 执行服务器 clean.sh
ssh dao "bash -s < /usr/local/projects/$directory_name/clean.sh"