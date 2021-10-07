package com.rick.security.demo.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rick
 * @createdAt 2021-09-17 16:18:00
 */
@RestController
public class LoginController {

    @GetMapping("oauth2/callback")
    public String callback(String code) {
        return code;
    }

    @GetMapping("homepage")
    public String homepage() {
        return "homepage";
    }

}
