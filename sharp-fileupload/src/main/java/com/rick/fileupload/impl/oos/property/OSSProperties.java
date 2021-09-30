package com.rick.fileupload.impl.oos.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:04:00
 */
@ConfigurationProperties(prefix = "fileupload.oss")
@Data
public class OSSProperties {

    private String endpoint;

    private String accessKeyId;

    private String accessKeySecret;

    private String bucketName;
}
