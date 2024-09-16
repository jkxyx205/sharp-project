package com.rick.meta.dict.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-06 18:18:00
 */
@ConfigurationProperties(prefix = "dict")
@Data
public class DictProperties {

    private List<Item> items = Collections.EMPTY_LIST;

    @Data
    public static class Item {

        private String type;

        private String sql;

        private Map<String, String> map;

        private List<String> list;
    }

    public Item getItemByType(String type) {
        return items.stream().filter(item -> item.getType().equals(type)).findFirst().get();
    }

}
