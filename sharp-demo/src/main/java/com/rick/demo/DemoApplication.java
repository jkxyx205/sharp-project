package com.rick.demo;

import com.rick.fileupload.client.controller.DocumentController;
import com.rick.fileupload.client.support.DocumentDAO;
import com.rick.fileupload.client.support.DocumentServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

/**
 * @author Rick
 * @createdAt 2021-10-13 11:58:00
 */
@SpringBootApplication
@DependsOn("entityDAOSupport")
@Import({DocumentDAO.class, DocumentServiceImpl.class, DocumentController.class})
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
