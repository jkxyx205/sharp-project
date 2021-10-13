package com.rick.fileupload.core.config;

import com.rick.common.util.ReflectUtils;
import com.rick.db.service.SharpService;
import com.rick.fileupload.client.support.DocumentDAO;
import com.rick.fileupload.client.support.DocumentServiceImpl;
import com.rick.fileupload.core.FileStore;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.core.support.FileUploadProperties;
import com.rick.fileupload.impl.local.LocalInputStreamStore;
import com.rick.fileupload.impl.local.property.LocalProperties;
import com.rick.fileupload.impl.oos.property.OSSProperties;
import com.rick.fileupload.plugin.image.ImageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.lang.reflect.Field;

/**
 * @author Rick
 * @createdAt 2021-09-29 14:06:00
 */
@Configuration
@EnableConfigurationProperties({FileUploadProperties.class, LocalProperties.class, OSSProperties.class})
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
     * 默认本地存储
     * @param localProperties
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public InputStreamStore inputStreamStore(LocalProperties localProperties) {
        return new LocalInputStreamStore(localProperties);
    }

    @Bean
    @Primary
    public FileStore fileStore(InputStreamStore inputStreamStore){
        return new FileStore(inputStreamStore);
    }

    @Bean
    public ImageService imageService(InputStreamStore inputStreamStore){
        return new ImageService(inputStreamStore);
    }

    @Bean
    public DocumentServiceImpl documentService(SharpService sharpService, FileStore fileStore,
                                               FileUploadProperties fileUploadProperties, ImageService imageService) {
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

        return new DocumentServiceImpl(documentDAO, fileStore, fileUploadProperties, imageService);
    }
//    @Configuration
//    static class FastDFSConfig {
        // 会先执行fastDFSUploaderHandler，再执行DefaultUploadHandler
//        @Bean
//        @Primary
//        public InputStreamStore fastDFSInputStreamStore() throws IOException, MyException {
//            return new FastDFSInputStreamStore("fdfs_client.properties");
//        }
//    }

//    @Configuration
//    static class OSSConfig {
 /*       @Bean
        @Primary*/
//        public InputStreamStore ossInputStreamStore(OSSProperties ossProperties) {
//            OSS ossClient = new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
//            return new OSSInputStreamStore(ossClient, ossProperties);
//        }
//    }
}
