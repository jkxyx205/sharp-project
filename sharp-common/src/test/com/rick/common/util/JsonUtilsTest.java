package com.rick.common.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.rick.common.model.Dept;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-06-02 19:13:00
 */
public class JsonUtilsTest {

    @Test
    public void testToJson() throws IOException {
        Dept dept = new Dept();
        dept.setId(1L);
        dept.setName("Dev");
        dept.setParentId(2L);
        String json = JsonUtils.toJson(dept);
        System.out.println(json);
    }

    @Test
    public void testToPOJO() throws IOException {
        Dept dept = JsonUtils.toObject("{\"id\":1,\"name\":\"Dev\",\"parentId\":2}", Dept.class);
        Assert.assertEquals(1L, dept.getId().longValue());
    }

    @Test
    public void testToMap() throws IOException {
        Map dept = JsonUtils.toObject("{\"id\":1,\"name\":\"Dev\",\"parentId\":2}", Map.class);
        // dept.get("id") Integer类型
        Assert.assertEquals(1, dept.get("id"));
    }

    @Test
    public void testToList() throws IOException {
        List list = JsonUtils.toObject("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]", List.class);
        // dept.get("id") Integer类型
        Assert.assertEquals(1, ((Map)list.get(0)).get("id"));
    }

    @Test
    public void testToListWithGenerics1() throws IOException {
        TypeReference<List<Dept>> typeRef = new TypeReference<List<Dept>>() {};
        List<Dept> list = JsonUtils.toObject("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]", typeRef);
        Assert.assertEquals(1L, list.get(0).getId().longValue());
    }

    @Test
    public void testToListWithGenerics2() throws IOException {
        List<Dept> list = JsonUtils.toList("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]", Dept.class);
        Assert.assertEquals(1L, list.get(0).getId().longValue());
    }

    @Test
    public void testListStringToJsonNode() throws IOException {
        JsonNode jsonNode = JsonUtils.toJsonNode("[{\"id\":1,\"name\":\"Dev\",\"parentId\":2}]");
        Assert.assertEquals(1L, jsonNode.get(0).get("id").longValue());
    }

    @Test
    public void testObjectStringToJsonNode() throws IOException {
        JsonNode jsonNode = JsonUtils.toJsonNode("{\"id\":1,\"name\":\"Dev\",\"parentId\":2}");
        Assert.assertEquals(1L, jsonNode.get("id").longValue());
    }

    @Test
    public void testObjectToJsonNode() throws IOException {
        Dept dept = new Dept();
        dept.setId(1L);
        dept.setName("Dev");
        dept.setParentId(2L);
        JsonNode jsonNode = JsonUtils.toJsonNode(dept);
        Assert.assertEquals(1L, jsonNode.get("id").asLong());
    }

    @Test
    public void testMapToJsonNode() {
        String id = "1";
        Map<String, String> params = new HashMap<>();
        params.put("id", id);
        JsonNode jsonNode = JsonUtils.toJsonNode(params);
        Assert.assertEquals(1L, jsonNode.get("id").asLong());
    }

}
