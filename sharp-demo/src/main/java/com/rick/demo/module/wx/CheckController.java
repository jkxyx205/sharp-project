package com.rick.demo.module.wx;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rick.Xu
 * @date 2023/10/31 17:45
 */
@RestController
@RequestMapping("wechat")
public class CheckController {

    @RequestMapping
    public String check(String signature, String timestamp, String nonce, String echostr) {
        System.out.println(signature);
        System.out.println(timestamp);
        System.out.println(nonce);
        System.out.println(echostr);
        return "true";
    }
}
