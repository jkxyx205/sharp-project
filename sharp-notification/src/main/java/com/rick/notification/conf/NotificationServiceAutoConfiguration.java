package com.rick.notification.conf;

import com.rick.notification.bark.DeviceKey;
import com.rick.notification.bark.PushNotificationService;
import com.rick.notification.fcm.MessagingSendService;
import com.rick.notification.telegram.TelegramSendService;
import com.rick.notification.wechat.TemplateMessageSender;
import com.rick.notification.wechat.TokenService;
import com.rick.notification.wechat.WechatKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 自动配置类
 * @author: Rick.Xu
 * @date: 4/12/25 7:42 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
@Configuration
public class NotificationServiceAutoConfiguration {

    public NotificationServiceAutoConfiguration() {
    }

    @Configuration
    @Import({DeviceKey.class, WechatKey.class})
    static class NotificationServiceConfiguration {

        @Bean
        public PushNotificationService pushNotificationService(DeviceKey deviceKey) {
            return new PushNotificationService(deviceKey);
        }

        @Bean
        public TemplateMessageSender templateMessageSender(WechatKey wechatKey) {
            return new TemplateMessageSender(new TokenService(wechatKey));
        }

        @Bean
        public MessagingSendService messagingSendService() {
            return new MessagingSendService();
        }

        @Bean
        public TelegramSendService telegramSendService() {
            return new TelegramSendService();
        }
    }
}
