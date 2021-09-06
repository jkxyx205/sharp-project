# 
## 添加依赖
```xml
    <dependency>
        <groupId>com.rick.meta</groupId>
        <artifactId>sharp-meta</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
```
## 创建脚本
```sql
create table sys_dict
(
type varchar(32) not null,
name varchar(32) not null,
label varchar(32) not null,
sort int null,
primary key (type, name)
)
charset=utf8mb4;

create table sys_property
(
name varchar(32) not null
primary key,
value varchar(255) not null
)
charset=utf8mb4;

INSERT INTO project_demo.sys_dict (type, name, label, sort) VALUES ('sex', 'F', '女', 1);
INSERT INTO project_demo.sys_dict (type, name, label, sort) VALUES ('sex', 'M', '男', 0);

INSERT INTO project_demo.sys_property (name, value) VALUES ('hello', 'world');
```

## 读取配置文件
`application-dict.yml`
```yml
dict:
  items:
    - type: user
      sql: "select username, name from sys_user order by id asc"
    - type: grade
      map: {g1: "一年级", g2: "二年级"}
```
## 创建测试
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class MetaTest {

    @Autowired
    private DictService dictService;

    @Autowired
    private PropertyService propertyService;

    @Test
    public void testList() {
        List<DictDO> sexList = dictService.getDictByType("sex");
        Assert.assertEquals(2, sexList.size());
    }

    @Test
    public void testGetOne() {
        DictDO dictDO = dictService.getDictByTypeAndName("sex", "F").get();
        Assert.assertEquals("女", dictDO.getLabel());
    }

    @Test
    public void testDictYml() {
        Assert.assertEquals(2, dictService.getDictByType("grade").size());
        Assert.assertEquals("一年级",dictService.getDictByTypeAndName("grade", "g1").get().getLabel());
        Assert.assertEquals("Rick",dictService.getDictByTypeAndName("user", "jkxyx205").get().getLabel());
        Assert.assertEquals("男",dictService.getDictByTypeAndName("sex", "M").get().getLabel());
    }

    @Test
    public void testGetProperty() {
        String property = propertyService.getProperty("hello");
        Assert.assertEquals("world", property);
    }

    @Test
    public void testSetProperty() {
        propertyService.setProperty("gg", "dd");
    }
}
```