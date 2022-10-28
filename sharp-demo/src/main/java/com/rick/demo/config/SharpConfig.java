package com.rick.demo.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.DefaultColumnAutoFill;
import com.rick.fileupload.core.InputStreamStore;
import com.rick.fileupload.impl.fastdfs.FastDFSInputStreamStore;
import com.rick.fileupload.impl.oos.OSSInputStreamStore;
import com.rick.fileupload.impl.oos.property.OSSProperties;
import lombok.RequiredArgsConstructor;
import org.csource.common.MyException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validator;
import java.io.IOException;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-03-23 23:58:00
 */
@Configuration
@RequiredArgsConstructor
public class SharpConfig {

    private final Validator validator;

    @Bean
    public ColumnAutoFill fill() {
        return new ColumnAutoFill() {
            @Override
            public Map<String, Object> insertFill(String idPropertyName, Long id) {
                Map<String, Object> fill = new DefaultColumnAutoFill().insertFill(idPropertyName, id);
                fill.put("created_by", 0L);
                fill.put("updated_by", 0L);
                fill.put("group_id", 99L);
                return fill;
            }

            @Override
            public Map<String, Object> updateFill() {
                Map<String, Object> fill = new DefaultColumnAutoFill().updateFill();
                fill.put("updated_by", 0L);
                return fill;
            }
        };
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
    public InputStreamStore fastDFSInputStreamStore() throws IOException, MyException {
        return new FastDFSInputStreamStore("fdfs_client.properties");
    }
}
