package com.rick.security.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Rick
 * @createdAt 2021-09-24 16:52:00
 */
@RestController
public class MessagesController {

    @GetMapping("/messages")
    public String[] getMessages() {
        String[] messages = new String[] {"Message 1", "Message 2", "Message 3"};
        return messages;
    }
}
