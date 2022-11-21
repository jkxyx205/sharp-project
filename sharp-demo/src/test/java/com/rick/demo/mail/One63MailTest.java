package com.rick.demo.mail;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Rick
 * @createdAt 2021-10-13 15:27:00
 */
@SpringBootTest
public class One63MailTest {
//
//    @Autowired
//    private MailHandler mailHandler;
//
//    @Test
//    public void testUnReadMessages() throws MessagingException {
//        mailHandler.listUnreadMessage(message -> {
//            try {
//                System.out.println(message.getSubject());
//                System.out.println("---------");
//                System.out.println(MailUtils.contentText(message.getContent()));
//                System.out.println("----contentText end-----");
//                System.out.println(MailUtils.hasAttachment(message));
//                System.out.println("---------");
//                System.out.println(MailUtils.downloadAttachments(message, "/Users/rick/Documents/mail"));
//                System.out.println("---------");
//            } catch (MessagingException | IOException e) {
//                e.printStackTrace();
//            }
//        });
//    }
//
//    @Test
//    public void testListMessages() throws MessagingException {
//        mailHandler.listMessages("INBOX", message -> {
//            try {
//                System.out.println(message.getSubject());
//                System.out.println("---------");
//                System.out.println(MailUtils.contentText(message.getContent()));
//                System.out.println("---------");
//                System.out.println(MailUtils.hasAttachment(message));
//                System.out.println("---------");
//                System.out.println(MailUtils.downloadAttachments(message, "/Users/rick/Documents/mail"));
//                System.out.println("---------");
//            } catch (MessagingException | IOException e) {
//                e.printStackTrace();
//            }
//            return true;
//        });
//    }
//
//    @Test
//    public void testSearchById() throws MessagingException, IOException {
//        Optional<MimeMessage> optional = mailHandler.searchByMessageId("INBOX", "<475871799.1.1668661823168@[192.168.2.7]>");
//        System.out.println(optional.isPresent() == false);
//
//        Optional<MimeMessage> optional2 = mailHandler.searchByMessageId("已发送", "<1605570842.1.1668738513305@[192.168.2.7]>");
//        System.out.println(optional2.get().getSubject());
//    }
//
//    @Test
//    public void testSenderSimple0() throws IOException, MessagingException {
//        MimeMessage message = mailHandler.send(Email.builder()
//                .subject("no attachment testSenderSimple0")
//                .from("jkxyx205@163.com", "Rick.Xu")
//                .to("xu.xu@yodean.com", "JIM")
//                .to("1050216579@qq.com")
//                .plainText("Here is an example to send a simple e-mail from your machine. It is assumed that your localhost is connected to the Internet and capable enough to send an e-mail.\n" +
//                        "\n")
//                .build());
//        System.out.println(message.getMessageID());
//    }
//
//    @Test
//    public void testSenderSimple() throws IOException, MessagingException {
//        mailHandler.send(Email.builder()
//                .subject("no attachment")
//                .from("jkxyx205@163.com", "Rick.Xu")
//                .to("xu.xu@yodean.com", "JIM")
//                .to("1050216579@qq.com")
//                .htmlText("hello <span style='color: red'>world</span>用户登录Webmail端企业邮箱，通过“设置>反垃圾/黑白名单”进入反垃圾/黑白名单页面。如下图所示，用户可自助设置垃圾邮件的判定规则、垃圾邮件的处理规则等。")
//                .build());
//    }
//    @Test
//    public void testSender() throws IOException, MessagingException {
//        mailHandler.send(Email.builder()
//                .subject("has attachment")
//                .from("jkxyx205@163.com", "Rick.Xu")
//                .to("xu.xu@yodean.com", "JIM")
//                .to("1050216579@qq.com")
//                .htmlText("hello <img src=\"cid:learn.png\" /><span style='color: red'>world</span>用户登录Webmail端企业邮箱，通过“设置>反垃圾/黑白名单”进入反垃圾/黑白名单页面。如下图所示，用户可自助设置垃圾邮件的判定规则、垃圾邮件的处理规则等。")
//                .embeddedImage("learn.png", Files.readAllBytes(Paths.get("/Users/rick/Desktop/learn.png")), "image/png")
//                .attachment("pom_extend.txt", Files.readAllBytes(Paths.get("/Users/rick/Desktop/pom_extend.txt")), "text/plain")
//                .attachment("部分合作单位.html", Files.readAllBytes(Paths.get("/Users/rick/Desktop/部分合作单位.html")), "text/html")
//                .build());
//    }
//
//    @Test
//    public void testSendAndSaveToOutbox() throws IOException, MessagingException {
//        MimeMessage message = mailHandler.sendAndSaveToOutbox(Email.builder()
//                .subject("Java - Sending Email-testSendAndSaveToOutBox---111")
//                .from("jkxyx205@163.com", "Rick.Xu")
//                .to("xu.xu@yodean.com", "JIM")
//                .to("1050216579@qq.com")
//                .cc("154894898@qq.com", "JK")
//                .htmlText("hello <img src=\"cid:learn.png\" /><span style='color: red'>world</span>用户登录Webmail端企业邮箱，通过“设置>反垃圾/黑白名单”进入反垃圾/黑白名单页面。如下图所示，用户可自助设置垃圾邮件的判定规则、垃圾邮件的处理规则等。")
//                .embeddedImage("learn.png", Files.readAllBytes(Paths.get("/Users/rick/Desktop/learn.png")), "image/png")
//                .attachment("pom_extend.txt", Files.readAllBytes(Paths.get("/Users/rick/Desktop/pom_extend.txt")), "text/plain")
//                .attachment("部分合作单位.html", Files.readAllBytes(Paths.get("/Users/rick/Desktop/部分合作单位.html")), "text/html")
//                .build());
//        System.out.println(message.getMessageID());
//    }
//
//    @Test
//    public void listFolder() throws MessagingException {
//        Folder[] folders = mailHandler.listFolders();
//        for (Folder folder : folders) {
//            System.out.println(folder.getFullName());
//        }
//
//        // store close() 下面会有异常
////        folders[2].open(Folder.READ_ONLY);
////        System.out.println(folders[2].getMessage(1).getSubject());
////        folders[2].close();
//    }

}
