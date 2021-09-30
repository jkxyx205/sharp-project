package com.rick.fileupload.core.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Rick
 * @createdAt 2021-09-29 18:22:00
 */
@ConfigurationProperties(prefix = "fileupload")
@Data
public class FileUploadProperty {

//    private boolean persist;

}
