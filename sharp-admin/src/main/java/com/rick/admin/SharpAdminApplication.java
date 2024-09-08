package com.rick.admin;

import com.rick.fileupload.client.controller.DocumentController;
import com.rick.fileupload.client.support.DocumentDAO;
import com.rick.fileupload.client.support.DocumentServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:09
 */
@SpringBootApplication
@EnableCaching
@DependsOn("entityDAOSupport")
@Import({DocumentDAO.class, DocumentServiceImpl.class, DocumentController.class})
public class SharpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(SharpAdminApplication.class, args);
    }

}
