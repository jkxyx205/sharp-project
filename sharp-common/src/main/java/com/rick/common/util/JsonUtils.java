package com.rick.common.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rick
 * @createdAt 2021-06-02 19:28:00
 */
public final class JsonUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
//        SimpleModule simpleModule = new SimpleModule();
//        simpleModule.addDeserializer(Enum.class, new EnumJsonDeserializer());
//        objectMapper.registerModule(simpleModule);

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(Include.NON_NULL);
//        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    private JsonUtils() {
    }

    @SneakyThrows
    public static String toJson(Object obj) {
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, obj);
        return writer.toString();
    }

    @SneakyThrows
    public static <T> T toObject(InputStream is, Class<T> clazz) {
        return  objectMapper.readValue(is, clazz);
    }

    @SneakyThrows
    public static <T> T toObject(String json, Class<T> clazz) {
        T t;
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        t = objectMapper.readValue(is, clazz);
        return t;
    }

    @SneakyThrows
    public static <T> T toObject(String json, TypeReference<T> typeRef)
            {
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        return objectMapper.readValue(is, typeRef);
    }

    @SneakyThrows
    public static <T> T toObject(JsonNode node, Class<T> clazz) {
        return objectMapper.treeToValue(node, clazz);
    }

    @SneakyThrows
    public static <T> List<T> toList(JsonNode node, Class<T> clazz) {
        return objectMapper.readerForListOf(clazz).readValue(node);
    }

    @SneakyThrows
    public static <T> List<T> toList(String json, Class<T> clazz) {
        return objectMapper.readerForListOf(clazz).readValue(json);
    }

    @SneakyThrows
    public static <T> Set<T> toSet(String json, Class<T> clazz) {
        JavaType javaType = getCollectionType(HashSet.class, clazz);
        return objectMapper.readValue(json, javaType);
    }

    @SneakyThrows
    public static <T> Set<T> toSet(JsonNode node) {
        return objectMapper.readerFor(new TypeReference<Set<T>>(){}).readValue(node);
    }

    @SneakyThrows
    public static <T> Object toObjectFromFile(String fileName, Class<T> clazz) {
        T obj;
        File file = new File(fileName);
        obj = objectMapper.readValue(file, clazz);
        return obj;
    }
    @SneakyThrows

    public static JsonNode toJsonNode(Object object) {
        return objectMapper.valueToTree(object);
    }

    @SneakyThrows
    public static JsonNode toJsonNode(String json) {
        JsonNode jsonNode = null;
        if (StringUtils.isNotEmpty(json)) {
            jsonNode = objectMapper.readValue(json, JsonNode.class);
        }
        return jsonNode;
    }

    public static Map<String, ?> objectToMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }

    public static String beautifyJSON(String json) {
        ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
        try {
            Object jsonObject = objectMapper.readValue(json, Object.class);
            return writer.writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

}