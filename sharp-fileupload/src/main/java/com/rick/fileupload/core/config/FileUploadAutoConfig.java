package com.rick.fileupload.core.config;

import com.rick.common.util.ReflectUtils;
import com.rick.db.service.SharpService;
import com.rick.fileupload.client.DocumentDAO;
import com.rick.fileupload.client.DocumentServiceImpl;
import com.rick.fileupload.core.FileStore;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.property.FileUploadProperty;
import com.rick.fileupload.core.property.LocalProperties;
import com.rick.fileupload.impl.local.InputStreamStoreImpl;
import com.rick.fileupload.plugin.image.ImageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.lang.reflect.Field;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:06:00
 */
@Configuration
@EnableConfigurationProperties({LocalProperties.class, FileUploadProperty.class})
public class FileUploadAutoConfig {

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsFilter(source);
    }

    /**
     * 本地存储
     * @param localProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public InputStreamStore inputStreamStore(LocalProperties localProperties) {
        return new InputStreamStoreImpl(localProperties);
    }

    @Bean
    public FileStore fileStore(InputStreamStore inputStreamStore){
        return new FileStore(inputStreamStore);
    }

    @Bean
    public ImageService imageService(InputStreamStore inputStreamStore){
        return new ImageService(inputStreamStore);
    }

    @Bean
//    @ConditionalOnExpression("#{${fileupload.persist} == true}")
    public DocumentServiceImpl documentService(SharpService sharpService, FileStore fileStore) {
        DocumentDAO documentDAO = new DocumentDAO();
        Field[] allFields = ReflectUtils.getAllFields(DocumentDAO.class);
        for (Field field : allFields) {
            if ("sharpService".equals(field.getName())) {
                field.setAccessible(true);
                try {
                    field.set(documentDAO, sharpService);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    break;
                }
            }
        }

        return new DocumentServiceImpl(documentDAO, fileStore);
    }

    @Configuration
    static class FastDFSConfig {
        // 会先执行fastDFSUploaderHandler，再执行DefaultUploadHandler
//        @Bean
//        public InputStreamStore fastDFSUploaderHandler() throws IOException, MyException {
//            return new FastDFSInputStreamStore("fdfs_client.properties");
//        }
    }


}
