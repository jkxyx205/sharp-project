package com.rick.fileupload.core.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:04:00
 */
@ConfigurationProperties(prefix = "fileupload.local")
@Data
public class LocalProperties {

    private String serverUrl;

    private String rootPath;

}
