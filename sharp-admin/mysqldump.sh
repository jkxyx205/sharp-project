#!/bin/bash

echo "---------------------------------------------------" >> /usr/local/projects/sharp-admin/mysqldump/dbBackLog.log
echo $(date +"%Y-%m-%d %H:%M:%S") "test Database backup start"  >> /usr/local/projects/sharp-admin/mysqldump/dbBackLog.log


mysqldump -uroot -p222222 --master-data=2 --single-transaction --databases product-manager > /usr/local/projects/sharp-admin/mysqldump/backup-$(date +"%Y-%m-%d").sql

if [ 0 -eq $? ];then

    if [ -f "/usr/local/projects/sharp-admin/mysqldump/backup-$(date +"%Y-%m-%d").sql" ];then
        echo $(date +"%Y-%m-%d %H:%M:%S") "test Database backup success!" >> /usr/local/projects/sharp-admin/mysqldump/dbBackLog.log
    else
        echo $(date +"%Y-%m-%d %H:%M:%S") "test Database backup fail!" >> /usr/local/projects/sharp-admin/mysqldump/dbBackLog.log
    fi

else
    echo $(date +"%Y-%m-%d %H:%M:%S") "test Database backup error!" >> /usr/local/projects/sharp-admin/mysqldump/dbBackLog.log

fi

echo "---------------------------------------------------" >> /usr/local/projects/sharp-admin/mysqldump/dbBackLog.log
find /usr/local/projects/sharp-admin/mysqldump/ -mtime +7 -name "backup-*.sql" -exec rm -rf {} \;
