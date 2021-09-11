package com.rick.sms.conf;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.rick.sms.core.AliSender;
import com.rick.sms.core.Sender;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 自动配置类
 * @author: Rick.Xu
 * @date: 9/12/19 7:42 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Configuration
public class SmsServiceAutoConfiguration {

    public SmsServiceAutoConfiguration() {
    }

    /**
     * 阿里短信服务AccessKey
     * @author Rick
     * @createdAt 2021-03-03 15:07:00
     */
    @Configuration
    @ConfigurationProperties(prefix = "ali.access-key")
    @Data
    public class AliAccessKey {

        private String id;

        private String secret;
    }

    @Configuration
    static class SmsServiceConfiguration {

        @Bean
        public IClientProfile getProfile(AliAccessKey aliAccessKey) {
            DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", aliAccessKey.getId(), aliAccessKey.getSecret());
            return profile;
        }

        @Bean
        public IAcsClient getClient(IClientProfile profile)  {
            return new DefaultAcsClient(profile);
        }

        @Bean
        public Sender getAliSender(IAcsClient client) {
            return new AliSender(client);
        }
    }
}
