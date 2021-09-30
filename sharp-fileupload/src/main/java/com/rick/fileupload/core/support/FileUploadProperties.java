package com.rick.fileupload.core.support;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Rick
 * @createdAt 2021-10-01 01:25:00
 */
@ConfigurationProperties(prefix = "fileupload")
@Data
public class FileUploadProperties {

    private String tmp;

}
