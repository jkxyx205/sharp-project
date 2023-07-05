package com.rick.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;

/**
 * @author Rick
 * @createdAt 2021-10-13 11:58:00
 */
@SpringBootApplication
@DependsOn("entityDAOSupport")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
