package com.rick.mail.core.mail;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-11-16 22:41:00
 */
public class EmailBuilder {

    private List<Object[]> toList = new ArrayList<>();

    private Object[] from = new Object[2];

    private String subject;

    private EmailBuilder() {}

    public static EmailBuilder builder() {
        return new EmailBuilder();
    }

    public EmailBuilder from(String address, String personal) {
        from[0] = address;
        from[1] = personal;
        return this;
    }

    public EmailBuilder to(String address, String personal) {
        // Message.RecipientType type, Address address
        toList.add(new Object[]{address, personal});
        return this;
    }

    public EmailBuilder cc(String address, String personal) {
        return this;
    }

    public EmailBuilder bcc(String address, String personal) {
        return this;
    }

    public EmailBuilder subject(String subject) {
        this.subject = subject;
        return this;
    }


}
