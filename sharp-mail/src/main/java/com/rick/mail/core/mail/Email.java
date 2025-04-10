package com.rick.mail.core.mail;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rick
 * @createdAt 2022-11-16 23:03:00
 */
@AllArgsConstructor
@Getter
public class Email {

    private String[] from;

    private List<Object[]> toList;

    private List<Object[]> ccList;

    private List<Object[]> bccList;

    private String subject;

    private String plainText;

    private String htmlText;

    private List<Object[]> attachmentList;

    private List<Object[]> embeddedImageList;

    public static EmailBuilder builder() {
        return new EmailBuilder();
    }

    public static class EmailBuilder {

        private String[] from = new String[2];

        private List<Object[]> toList = new ArrayList<>();

        private List<Object[]> ccList = new ArrayList<>();

        private List<Object[]> bccList = new ArrayList<>();

        private String subject;

        private String plainText;

        private String htmlText;

        private List<Object[]> attachmentList = new ArrayList<>();

        private List<Object[]> embeddedImageList = new ArrayList<>();

        public EmailBuilder from(String address, String personal) {
            from[0] = address;
            from[1] = personal;
            return this;
        }

        public EmailBuilder to(String address) {
            return to(address, null);
        }

        public EmailBuilder to(String address, String personal) {
            toList.add(new Object[]{address, personal});
            return this;
        }

        public EmailBuilder cc(String address) {
            return cc(address, null);
        }

        public EmailBuilder cc(String address, String personal) {
            ccList.add(new Object[]{address, personal});
            return this;
        }

        public EmailBuilder bcc(String address) {
            return bcc(address, null);
        }

        public EmailBuilder bcc(String address, String personal) {
            bccList.add(new Object[]{address, personal});
            return this;
        }

        public EmailBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailBuilder plainText(String plainText) {
            this.plainText = plainText;
            return this;
        }

        public EmailBuilder htmlText(String htmlText) {
            this.htmlText = htmlText;
            return this;
        }

        public EmailBuilder attachment(String fileName, byte[] byteArray, String type) {
            this.attachmentList.add(new Object[]{fileName, byteArray, type});
            return this;
        }

        public EmailBuilder attachments(List<Object[]> attachments) {
            for (Object[] attachment : attachments) {
                this.attachmentList.add(new Object[]{attachment[0], attachment[1], attachment[2]});
            }
            return this;
        }

        public EmailBuilder embeddedImage(String fileName, byte[] byteArray, String type) {
            this.embeddedImageList.add(new Object[]{fileName, byteArray, type});
            return this;
        }

        public Email build() {
            return new Email(this.from, this.toList, this.ccList, this.bccList, this.subject, this.plainText,
                    this.htmlText, this.attachmentList, this.embeddedImageList);
        }
    }
}
