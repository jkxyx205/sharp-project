# 部署运行
1. 下载代码
```shell
git clone https://github.com/jkxyx205/sharp-project.git
```
2. 修改数据库配置 `application-dev.yml` `端口` 等
3. 安装数据库，从 `init-sql.sql` 导入数据 


2. 编译依赖
```shell
cd sharp-project
sh mvn.sh
```
3。 编译 sharp-admin
```shell
cd sharp-admin
sh mvn
```
4. 上传服务器（安装JDK）
```shell
nohup java -Xms64m -Xmx1024m -Dspring.profiles.active=dev -jar sharp-admin-1.0-SNAPSHOT.jar --server.port=8081 &
```
5. 域名解析到服务器
6. 安装 nginx
7. 阿里云申请免费证书， 下载 nginx 下的证书
8. nginx 配置证书
```
    server {
         listen 443 ssl;
     
         server_name sharp-admin.xhope.top;
 
         ssl_certificate /usr/local/ssl/sharp-admin.xhope.top.pem;
         ssl_certificate_key /usr/local/ssl/sharp-admin.xhope.top.key;
 
         ssl_session_cache shared:SSL:1m;
         ssl_session_timeout 5m;
     
         ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:ECDHE:ECDH:AES:HIGH:!NULL:!aNULL:!MD5:!ADH:!RC4;
         ssl_protocols TLSv1.1 TLSv1.2 TLSv1.3;

         ssl_prefer_server_ciphers on;
 
         location / {
               proxy_pass http://localhost:8081/;
         }
    }
    
    server {
        listen 80;
        server_name sharp-admin.xhope.top;
        rewrite ^(.*)$ https://$host$1;
        location / {
               proxy_pass http://localhost:8081/;
        }
    }
```
## 自动部署
0. 安装数据库
1. 在服务器创建 `deploy` 文件夹用于上传 jar 文件
```shell
cd sharp-admin
mkdir deploy
```
2. 修改 `config.sh` 和 `mysqldump.sh` 后，和 `deploy.sh` 一并拷贝到服务器项目目录下
目录结构：
```
sharp-admin
├── config.sh // 配置目录
├── deploy // jar 临时存放的目录
├── deploy.sh // 部署命令
├── mysqldump // 备份文件
└── mysqldump.sh // 备份脚步
```
3. 运行 `mvn.sh` （服务器进行免密登录配置）
## 数据库自动备份
运行 `crontab -e`
```shell
#00 22 * * * mysqldump -uroot -p222222 --master-data=2 --single-transaction --databases product-manager > /usr/local/projects/sharp-admin/mysqldump/backup-$(date +'\%F-\%R').sql

00 22 * * * sh /usr/local/projects/sharp-admin/mysqldump.sh
```
详情可以参考：[https://xhope.top/?p=1722](https://xhope.top/?p=1722)
## Docker 部署
1. 安装 `Docker` 环境
2. 执行命令
```shell
#!/bin/bash
mvn clean package -Dmaven.test.skip=true
docker-compose down
docker-compose up -d
```
3. 应用程序更新
```shell
#!/bin/bash
docker-compose stop sharp-admin
docker-compose rm sharp-admin -f
mvn clean package -Dmaven.test.skip=true
docker rmi sharp-admin:2.0
docker build -t sharp-admin:2.0 .
docker-compose create sharp-admin
docker-compose start sharp-admin
```
# 如何增删改查1
1. 创建 `实体`
2. 创建 `DAO`
3. 生成表结构
4. 创建form， 获取id
5. 创建report， 获取id， 关联form_id
6. 配置权限
7. 添加菜单

# 如何增删改查2
## 创建 实体类
`Student.java`
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "t_student", comment = "学生表")
public class Student extends BaseEntity {

    String name;
}
```

## 生成表
```java
tableGenerator.createTable(Student.class);
```

## 生成 Report
```java
  @Test
  public void testReport() {
      reportService.saveOrUpdate(Report.builder()
              .code("t_student")// 　建议和数据库表名保持一致
              .tplName("demos/student/list") // 拷贝模版页面到指定目录
//                .tplName("tpl/list/list") // 没有特殊要求使用模版页面
//                .tplName("tpl/list/ajax_list") // 没有特殊要求使用模版页面
              .name("学生信息")
                      .additionalInfo(Params.builder(1).pv("operator-bar", true) // 显示操作按钮
                              .pv("endpoint", "students")
                              .build()) // 显示操作按钮
              .querySql("select id, name, create_time from t_student where name like :name and is_deleted = 0")
              .queryFieldList(Arrays.asList(
                      new QueryField("name", "姓名")
              ))
              .reportColumnList(Arrays.asList(
                      new HiddenReportColumn("id"),
                      new ReportColumn("name", "名称", true),
                      new ReportColumn("create_time", "创建时间", false,null, Arrays.asList("localDateTimeConverter"))
              ))
              .pageable(true)
              .sidx("create_time")
              .sord(SordEnum.DESC)
              .build());
  }
```
获取 report id = 858412707060166656

## 配置 index.html 菜单
```html
<li class="nav-item">
    <a class="nav-link" href="javascript:addTab('student', '测试student', '/reports/858412707060166656')">
        <i class="nav-icon icon-home"></i> 测试student
    </a>
</li>
```
## 添加Form
拷贝 `tpl/edit.html`
```html
$.ajax ({
   url: "/endpoint", // 修改一处，改成自己的 endpoint
         type: "post",
         data: JSON.stringify(formData),
         dataType: "json",
         contentType: "application/json; charset=utf-8",
         success: function(data){
               if (!data.success) {
                   toastr.error(data.message)
               } else {
                   idDOM.value = res.data.id
                   toastr.success("保存成功")
                   toastr.success("保存成功")
               }
           }
     });
```
# 如何增删改查3