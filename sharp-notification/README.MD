```text
    PushNotificationService pushNotificationService;

    @Autowired
    MessagingSendService messagingSendService;

    @Autowired
    TemplateMessageSender templateMessageSender;

    @Autowired
    TelegramSendService telegramSendService;

    /**
     * bark:
     *   url:  ${BARK.URL} # 这个地址不允许批量发送消息
     *   device-key: ${BARK.KEY}
     */
    @Test
    public void testBark() {
        pushNotificationService.push(PushNotification.builder()
                .title("服务器异常通知")
                .subtitle("subtitle")
                .body("body")
                .badge(1)
                .sound("alarm")
                .icon("https://day.app/assets/images/avatar.jpg")
                .group("default")
                .url("https://challenge.com/")
                .build());
    }

    /**
     * 客户端获取token
     */
    @Test
    public void testFCM() {
        messagingSendService.send("token",
                "title", "message");
    }

    /**
     * whitelist:发送的机器要加入白名单
     * wechat:
     *   appid:
     *   secret:
     */
    @Test
    public void testWechat() {
        // 发送模版消息
        Map<String, String> data = new HashMap<>(3);
        data.put("thing7", "code");
        // 最多20个字符
        data.put("thing8", "type");
        templateMessageSender.send("oNB4Jv-xdFE31idz5LwUDpD-WS6E", "QXVUgqdISx-AhKJNj6Ei2qmyQQJmeF2kPyarWe3Cl-M", data);
    }

    @Test
    public void testTelegram() {
        String url = "https://api.telegram.org/bot81287317890:Cs/sendMessage?chat_id=12345567876&text=hello world";
        telegramSendService.send(url);
    }
```