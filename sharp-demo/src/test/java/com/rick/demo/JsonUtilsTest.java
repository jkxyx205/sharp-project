package com.rick.demo;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.rick.common.util.JsonUtils;
import com.rick.demo.module.project.domain.entity.Dept;
import com.rick.demo.module.project.domain.enums.TestCodeEnum;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Rick
 * @createdAt 2021-06-02 19:13:00
 */
public class JsonUtilsTest {

    @Test
    public void testListList() {
        List<List<String>> list = new ArrayList<>();
        list.add(Arrays.asList("1", "2"));
        list.add(Arrays.asList("3", "4"));

        System.out.println(JsonUtils.toJson(list));
        System.out.println(JsonUtils.toList("[[\"1\",\"2\"],[\"3\",\"4\"]]", List.class));
        System.out.println(JsonUtils.toList("[[\"足球\", \"篮球\"]]", List.class));
    }


    @Test
    public void testToJson() {
        Dept dept = new Dept();
        dept.setId(1L);
        dept.setName("Dev");
        dept.setParentId(2L);
        String json = JsonUtils.toJson(dept);
        System.out.println(json);
    }

    @Test
    public void testToPOJO() {
        Dept dept = JsonUtils.toObject("{\"id\":1,\"name\":\"Dev\",\"parent_id\":2}", Dept.class);
        assertThat(dept.getId().longValue()).isEqualTo(1L);
    }

    @Test
    public void testToMap() {
        Map dept = JsonUtils.toObject("{\"id\":1,\"name\":\"Dev\",\"parent_id\":2}", Map.class);
        // dept.get("id") Integer类型
        assertThat(dept.get("id")).isEqualTo(1);
    }

    @Test
    public void testToList() {
        List list = JsonUtils.toObject("[{\"id\":1,\"name\":\"Dev\",\"parent_id\":2}]", List.class);
        // dept.get("id") Integer类型
        assertThat(((Map)list.get(0)).get("id")).isEqualTo(1);

    }

    @Test
    public void testToListWithGenerics1() {
        TypeReference<List<Dept>> typeRef = new TypeReference<List<Dept>>() {};
        List<Dept> list = JsonUtils.toObject("[{\"id\":1,\"name\":\"Dev\",\"parent_id\":2}]", typeRef);
        assertThat(list.get(0).getId().longValue()).isEqualTo(1L);
        assertThat(list.get(0).getName()).isEqualTo("Dev");
        assertThat(list.get(0).getParentId()).isEqualTo(2L);
    }

    @Test
    public void testToListWithGenerics2() {
        List<Dept> list = JsonUtils.toList("[{\"id\":1,\"name\":\"Dev\",\"parent_id\":2}]", Dept.class);
        assertThat(list.get(0).getId().longValue()).isEqualTo(1L);
    }

    @Test
    public void testListStringToJsonNode() {
        JsonNode jsonNode = JsonUtils.toJsonNode("[{\"id\":1,\"name\":\"Dev\",\"parent_id\":2}]");
        assertThat(jsonNode.get(0).get("id").longValue()).isEqualTo(1L);
    }

    @Test
    public void testObjectStringToJsonNode() {
        JsonNode jsonNode = JsonUtils.toJsonNode("{\"id\":1,\"name\":\"Dev\",\"parent_id\":2}");
        assertThat(jsonNode.get("id").longValue()).isEqualTo(1L);
    }

    @Test
    public void testObjectToJsonNode() {
        Dept dept = new Dept();
        dept.setId(1L);
        dept.setName("Dev");
        dept.setParentId(2L);
        JsonNode jsonNode = JsonUtils.toJsonNode(dept);
        assertThat(jsonNode.get("id").asLong()).isEqualTo(1L);
    }

    @Test
    public void testMapToJsonNode() {
        String id = "1";
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        JsonNode jsonNode = JsonUtils.toJsonNode(params);
        assertThat(jsonNode.get("id").asLong()).isEqualTo(1L);
    }

    @Test
    public void testCodeEnum() {
        List<TestCodeEnum> TestCodeList1 = JsonUtils.toList("[\"11\", \"10\"]", TestCodeEnum.class);
        System.out.println(TestCodeList1);

        List<TestCodeEnum> TestCodeList2 = JsonUtils.toList("[\"0\", \"1\"]", TestCodeEnum.class);
        System.out.println(TestCodeList2);

        // 有@JsonValue 下面执行报错
//        List<TestCodeEnum> TestCodeList3 = JsonUtils.toList("[\"UNKNOWN\", \"CODE\"]", TestCodeEnum.class);
//        System.out.println(TestCodeList3);
    }

}
