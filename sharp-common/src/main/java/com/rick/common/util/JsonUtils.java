package com.rick.common.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
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
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    private JsonUtils() {
    }

    public static String toJson(Object obj) throws IOException {
        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, obj);
        return writer.toString();
    }

    public static <T> T toObject(InputStream is, Class<T> clazz) throws IOException {
        return  objectMapper.readValue(is, clazz);
    }

    public static <T> T toObject(String json, Class<T> clazz) throws IOException {
        T t;
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        t = objectMapper.readValue(is, clazz);
        return t;
    }

    public static <T> T toObject(String json, TypeReference<T> typeRef)
            throws IOException {
        InputStream is = new ByteArrayInputStream(json.getBytes("UTF-8"));
        return objectMapper.readValue(is, typeRef);
    }

    public static <T> T toObject(JsonNode node, Class<T> clazz)
            throws JsonProcessingException {
        return objectMapper.treeToValue(node, clazz);
    }

    public static <T> List<T> toList(JsonNode node, Class<T> clazz) throws IOException {
        return objectMapper.readerForListOf(clazz).readValue(node);
    }

    public static <T> List<T> toList(String json, Class<T> clazz) throws IOException {
        return objectMapper.readerForListOf(clazz).readValue(json);
    }

    public static <T> Set<T> toSet(String json, Class<T> clazz) throws IOException {
        JavaType javaType = getCollectionType(HashSet.class, clazz);
        return objectMapper.readValue(json, javaType);
    }

    public static <T> Set<T> toSet(JsonNode node) throws IOException {
        return objectMapper.readerFor(new TypeReference<Set<T>>(){}).readValue(node);
    }

    public static <T> Object toObjectFromFile(String fileName, Class<T> clazz) throws IOException {
        T obj;
        File file = new File(fileName);
        obj = objectMapper.readValue(file, clazz);
        return obj;
    }

    public static JsonNode toJsonNode(Object object) {
        return objectMapper.valueToTree(object);
    }

    public static JsonNode toJsonNode(String json) throws IOException {
        JsonNode jsonNode = null;
        if (StringUtils.isNotEmpty(json)) {
            jsonNode = objectMapper.readValue(json, JsonNode.class);
        }
        return jsonNode;
    }

    public static Map<String, ?> objectToMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }

    private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

}