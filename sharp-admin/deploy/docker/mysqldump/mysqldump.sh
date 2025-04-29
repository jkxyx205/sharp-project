#!/bin/bash

# 1. 定义目录
SCRIPT_DIR=$(cd $(dirname ${BASH_SOURCE[0]}); pwd)
cd ${SCRIPT_DIR}

# 2. 数据库连接信息
username="root"
password="123456"
database="sharp-admin"

# 3. 日志记录
LOG_FILE="${SCRIPT_DIR}/dbBackLog.log"

echo "---------------------------------------------------" >> ${LOG_FILE}
echo "$(date +"%Y-%m-%d %H:%M:%S") Database backup start" >> ${LOG_FILE}

# 4. 备份文件名
BACKUP_FILE="backup-$(date +"%Y-%m-%d").sql"

# 5. 执行备份
mysqldump -u${username} -p${password} --master-data=2 --single-transaction --databases ${database} > ${BACKUP_FILE} 2>>${LOG_FILE}

# 6. 判断备份是否成功
if [ $? -eq 0 ]; then
    if [ -f "${BACKUP_FILE}" ]; then
        echo "$(date +"%Y-%m-%d %H:%M:%S") Database backup success!" >> ${LOG_FILE}
    else
        echo "$(date +"%Y-%m-%d %H:%M:%S") Database backup fail (file not found)!" >> ${LOG_FILE}
    fi
else
    echo "$(date +"%Y-%m-%d %H:%M:%S") Database backup error (mysqldump failed)!" >> ${LOG_FILE}
fi

# 7. 清理7天前的旧备份，只清理当前目录下的 backup-*.sql，排除/proc /sys /dev
find ${SCRIPT_DIR} -maxdepth 1 -type f -name "backup-*.sql" -mtime +7 -exec rm -f {} \;

echo "$(date +"%Y-%m-%d %H:%M:%S") Old backups cleared." >> ${LOG_FILE}
echo "---------------------------------------------------" >> ${LOG_FILE}
