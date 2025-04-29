# -*- coding: utf-8 -*-
import glob
import os
import oss2
from oss2.credentials import EnvironmentVariableCredentialsProvider

directory = '/usr/local/projects/sharp-admin/mysqldump/'

files = glob.glob(os.path.join(directory, 'backup-*.sql'))

if not files:
    print("没有找到备份文件")
else:
    latest_file = max(files, key=os.path.getmtime)
    print("最新的备份文件是:", latest_file)

# 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
auth = oss2.ProviderAuthV4(EnvironmentVariableCredentialsProvider())

# 填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
endpoint = "https://oss-cn-beijing.aliyuncs.com"

# 填写Endpoint对应的Region信息，例如cn-hangzhou。注意，v4签名下，必须填写该参数
region = "cn-beijing"
# 填写Bucket名称，例如examplebucket。
bucketName = "sqldump-all"
# 创建Bucket实例，指定存储空间的名称和Region信息。
bucket = oss2.Bucket(auth, endpoint, bucketName, region=region)

# 使用put_object_from_file方法将本地文件上传至OSS
bucket.put_object_from_file("py/" + os.path.basename(latest_file), latest_file)