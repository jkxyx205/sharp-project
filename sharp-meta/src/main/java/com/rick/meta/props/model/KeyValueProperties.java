package com.rick.meta.props.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-06 18:18:00
 */
@ConfigurationProperties(prefix = "props")
@Data
public class KeyValueProperties {

    private Map<String, String> items = new HashMap<>();

}
