#!/bin/bash
source ./config.sh

# 获取当前工作目录的绝对路径
current_dir=$(pwd)

mvn clean package -Dmaven.test.skip=true
scp $current_dir/target/$project_name-1.0-SNAPSHOT.jar root@$server_ip:$dir/$project_name/deploy

#ssh dao
# cd /usr/local/projects/dao
# sh deploy.sh
#ps -ef | pgrep -f "8082"

# 服务器免密登录 ssh dao
# 执行服务器 deploy.sh
ssh dao "cd $dir/$project_name && bash -s < $dir/$project_name/deploy.sh"
