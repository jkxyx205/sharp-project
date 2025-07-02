package com.rick.demo.notification;

import com.rick.notification.bark.PushNotification;
import com.rick.notification.bark.PushNotificationService;
import com.rick.notification.fcm.MessagingSendService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Rick.Xu
 * @date 2025/4/27 15:28
 */
@SpringBootTest
public class PushNotificationServiceTest {

    @Autowired
    PushNotificationService pushNotificationService;

    @Autowired
    MessagingSendService messagingSendService;

    @Test
    public void pushTest() {
        pushNotificationService.push(PushNotification.builder()
                .title("错误通知")
                .subtitle("采购申请发起失败")
                .body("简介. 使用SmsForwarder和Bark将安卓手机上的短信、通话记录、应用通知等通过自建服务器bark-server转发到IOS设备上，免去外出双持烦恼。")
                .badge(1)
                .sound("alarm")
                .icon("https://day.app/assets/images/avatar.jpg")
                .group("default")
                .url("https://xhope.top/")
                .build());
    }

    @Test
    public void sendTest() {
        messagingSendService.send("f3dNejKHRmGynV3kEi_qF8:APA91bHGvOya-LSJCDPWEsV0QBO_UjqQ3vw_7RnJ9T0ofKyWTEYvvpBfhmD2fI7n1HaDcx1sl6kXT-DvNQNaee7-1y6W1RJ0nHnPZKcUIxcvOqIFUjBmQpM",
                "你好", "我是李秀妍！！");
    }
}
