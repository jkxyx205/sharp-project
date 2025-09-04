package com.rick.demo.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.rick.common.validate.ValidatorHelper;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.impl.fastdfs.FastDFSInputStreamStore;
import com.rick.fileupload.impl.oos.MinioInputStreamStore;
import com.rick.fileupload.impl.oos.OSSInputStreamStore;
import com.rick.fileupload.impl.oos.property.OSSProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.csource.common.MyException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.validation.Validator;
import java.io.IOException;

/**
 * @author Rick
 * @createdAt 2021-03-23 23:58:00
 */
@Configuration
@RequiredArgsConstructor
public class SharpConfig {

    private final Validator validator;

    @Bean
    public ExtendTableDAOImpl tableDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new ExtendTableDAOImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public ValidatorHelper validatorHelper() {
        return new ValidatorHelper(validator);
    }

//    @Bean
    public InputStreamStore ossInputStreamStore(OSSProperties ossProperties) {
        OSS ossClient = new OSSClientBuilder().build(ossProperties.getEndpoint(), ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret());
        return new OSSInputStreamStore(ossClient, ossProperties);
    }

//    @Bean
    public InputStreamStore minioInputStreamStore(OSSProperties ossProperties) {
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(ossProperties.getEndpoint())
                        .credentials(ossProperties.getAccessKeyId(), ossProperties.getAccessKeySecret())
                        .build();
        return new MinioInputStreamStore(minioClient, ossProperties);
    }

//    @Bean
    public InputStreamStore fastDFSInputStreamStore() throws IOException, MyException {
        return new FastDFSInputStreamStore("fdfs_client.properties");
    }
}
