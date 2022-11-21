package com.rick.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Rick
 * @createdAt 2022-11-16 21:48:00
 */
@ConfigurationProperties(prefix = "spring.mail.imap")
@Data
public class ImapMailProperties {

    private String host;

    private Integer port;
}
