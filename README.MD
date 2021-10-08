# sharp-database
`sharp-database` 是一个持久化操作工具的java实现，主要做查询功能。底层依赖 `JdbcTemplate` 。可以结合 `MyBatis` 的动态SQL一起使用。`sharp-database` 作为数据库操作工具的补充，不是用来了替换 `MyBatis` 或 `JPA`。目前支持 `Mysql` 和 `Oracle` 两种数据库，默认支持 `Mysql`。

`pom.xml`
```xml
        <dependency>
            <groupId>com.rick.db</groupId>
            <artifactId>sharp-database</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
```
`application.xml`
```yml
sharp:
  database:
    type: oracle
```
更多用法：
* [https://xhope.top/?p=1170](https://xhope.top/?p=1170)
* [https://xhope.top/?p=1266](https://xhope.top/?p=1266)

# sharp-excel
`sharp-excel` 是一个操作Excel的java实现。依赖apache的POI。

`pom.xml`
```xml
    <dependency>
        <groupId>com.rick.office</groupId>
        <artifactId>sharp-excel</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
```
更多用法：
* [https://xhope.top/?p=1220](https://xhope.top/?p=1220)
* [https://xhope.top/?p=1229](https://xhope.top/?p=1229)

# sharp-meta
`sharp-meta`可以对「字典」和「KV属性」进行读取和操作。

更多用法：
* [https://xhope.top/?p=1242](https://xhope.top/?p=1242)

# sharp-fileupload
`sharp-fileupload` 可以上传文件，对图片的处理（裁剪、选装、缩放）。提供了Restfull接口上传，访问文档。底层存储功能支持：

* 本地存储
* 阿里云OSS
* FastDFS

更多用法：

* [https://xhope.top/?p=1280](https://xhope.top/?p=1280)
* [https://xhope.top/?p=1284](https://xhope.top/?p=1284)
* [https://xhope.top/?p=1286](https://xhope.top/?p=1286)
* [https://xhope.top/?p=1288](https://xhope.top/?p=1288)