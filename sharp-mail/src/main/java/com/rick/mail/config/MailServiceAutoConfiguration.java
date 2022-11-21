package com.rick.mail.config;

import com.rick.mail.core.MailHandler;
import com.rick.mail.core.MailHandlerImpl;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * @author Rick
 * @createdAt 2022-11-16 21:17:00
 */
@Configuration
@EnableConfigurationProperties({ImapMailProperties.class})
@AutoConfigureAfter(MailSenderAutoConfiguration.class)
public class MailServiceAutoConfiguration {

    @Bean
    public MailHandler createSharpSenderImpl(JavaMailSender sender, MailProperties mailProperties, ImapMailProperties imapMailProperties){
        return new MailHandlerImpl(sender, mailProperties, imapMailProperties);
    }
}
