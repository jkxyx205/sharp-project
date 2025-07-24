package com.rick.mail.core;

import com.rick.mail.config.ImapMailProperties;
import com.rick.mail.core.mail.Email;
import com.sun.mail.imap.IMAPStore;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;
import javax.mail.util.ByteArrayDataSource;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Rick
 * @createdAt 2022-11-16 21:57:00
 */
@RequiredArgsConstructor
public class MailHandlerImpl implements MailHandler {

    private static final int SIZE = 50;

    private final JavaMailSender sender;

    private final MailProperties mailProperties;

    private final ImapMailProperties imapMailProperties;

    private Store cachedStore;

    @Override
    public MimeMessage send(Email email) throws MailException {
        return send(convertEmailToMimeMessage(email));
    }

    @Override
    public MimeMessage sendAndSaveToOutbox(Email email) {
        return sendAndSaveToOutbox(convertEmailToMimeMessage(email));
    }

    @Override
    public MimeMessage send(MimeMessage message) {
        sender.send(message);
        return message;
    }

    @Override
    public MimeMessage sendAndSaveToOutbox(MimeMessage message) {
        sender.send(message);
        saveToOutbox(message);
        return message;
    }

    @Override
    public Optional<MimeMessage> searchByMessageId(String folderName, String messageId) throws MessagingException {
        Store store = getStore();
        Folder folder = store.getFolder(folderName);
        return searchByMessageId(folder, messageId);
    }

    @Override
    public Optional<MimeMessage> searchByMessageId(Folder folder, String messageId) throws MessagingException {
        //TODO 163 读取有问题
        SearchTerm searchTerm = new MessageIDTerm(messageId);
        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }
        Message[] messages = folder.search(searchTerm);

        MimeMessage message = messages.length > 0 ? (MimeMessage) messages[0] : null;

        if (Objects.nonNull(message)) {
            message.getMessageID(); // get data from server execute loadEnvelope()
        }
        folder.close();
        this.closeStore();
        return Optional.ofNullable(message);
    }

    @Override
    public Folder[] listFolders() throws MessagingException {
        Store store = getStore();
        Folder defaultFolder = store.getDefaultFolder();
        Folder[] list = defaultFolder.list();
        store.close(); // 关闭store后，163下folder不能读取邮件，需要手动关闭。
        return list;
    }

    @Override
    public void listUnreadMessage(Consumer<MimeMessage> consumer) throws MessagingException {
        Store store = getStore();
        Folder inbox = store.getFolder("INBOX");
        int unreadMessageCount = inbox.getUnreadMessageCount();

        if (unreadMessageCount > 0) {
            // 遍历所有未读邮件
            AtomicLong atomicLong = new AtomicLong(0);
            inbox.open(Folder.READ_WRITE);
            listMessages(inbox, message -> {
                if (atomicLong.get() >= unreadMessageCount) {
                    return true;
                }

                try {
                    if (!message.getFlags().contains(Flags.Flag.SEEN)) {
                        atomicLong.getAndIncrement();
                        consumer.accept(message);
                        message.setFlag(Flags.Flag.SEEN, true);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                    return false;
                }

                return false;
            });
        }

        store.close();
        // inbox.close(); // listMessages已关闭
    }

    @Override
    public void listMessages(String folderName, Function<MimeMessage, Boolean> consumer) throws MessagingException {
        Assert.notNull(consumer, "consumer cannot be null");
        Store store = getStore();
        Folder folder = store.getFolder(folderName);
        listMessages(folder, consumer);
        store.close();
    }

    @Override
    public void listMessages(Folder folder, Function<MimeMessage, Boolean> consumer) throws MessagingException {
        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }

        int end = folder.getMessageCount();
        int start = (end - SIZE + 1) > 1 ? (end - SIZE + 1) : 1;
        while (end > 0) {
            Message[] messages = folder.getMessages(start, end);

            for (int i = messages.length - 1; i >= 0; i--) {
                MimeMessage message = (MimeMessage) messages[i];
                if (consumer.apply(message)) {
                    start = 1;
                    break;
                }
            }

            end = start - 1;
            start = (end - SIZE + 1) > 1 ? (end - SIZE + 1) : 1;
        }

        folder.close();
    }

    private MimeMessage convertEmailToMimeMessage(Email email) throws MailException {
        try {
            MimeMessage message = sender.createMimeMessage();
            message.setFrom(new InternetAddress(email.getFrom()[0], email.getFrom()[1]));
            for (Object[] to : email.getToList()) {
                message.addRecipient(Message.RecipientType.TO,new InternetAddress((String) to[0], (String) to[1]));
            }

            for (Object[] cc : email.getCcList()) {
                message.addRecipient(Message.RecipientType.CC,new InternetAddress((String) cc[0], (String) cc[1]));
            }

            for (Object[] bcc : email.getBccList()) {
                message.addRecipient(Message.RecipientType.BCC,new InternetAddress((String) bcc[0], (String) bcc[1]));
            }

            message.setSubject(email.getSubject());

            // text
            MimeBodyPart textPart = null;
            if (StringUtils.hasText(email.getPlainText())) {
                textPart = new MimeBodyPart();
                textPart.setText(email.getPlainText(), "UTF-8", "plain");
                message.setText(email.getPlainText(), "UTF-8", "plain");
            } else if (StringUtils.hasText(email.getHtmlText())) {
                textPart = new MimeBodyPart();
                textPart.setText(email.getHtmlText(), "UTF-8", "html");
                message.setText(email.getHtmlText(), "UTF-8", "html");
            }

            Multipart multipart = new MimeMultipart();
            if (textPart != null) {
                multipart.addBodyPart(textPart);
            }

            // attachments
            for (Object[] attachment : email.getAttachmentList()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setFileName(MimeUtility.encodeText((String) attachment[0]));
                DataSource source = new ByteArrayDataSource((byte[]) attachment[1], (String) attachment[2]);
                attachmentPart.setDataHandler(new DataHandler(source));
                multipart.addBodyPart(attachmentPart);
            }

            // embeddedImage
            for (Object[] embeddedImage : email.getEmbeddedImageList()) {
                MimeBodyPart embeddedImagePart = new MimeBodyPart();
                embeddedImagePart.setFileName((String) embeddedImage[0]);
                embeddedImagePart.setContentID(embeddedImagePart.getFileName());
                embeddedImagePart.setDisposition(MimeBodyPart.INLINE);
                DataSource source = new ByteArrayDataSource((byte[]) embeddedImage[1], (String) embeddedImage[2]);
                embeddedImagePart.setDataHandler(new DataHandler(source));
                multipart.addBodyPart(embeddedImagePart);
            }

            // has attachments or embeddedImage：content is multipart, else is string & type = text/*
            // Content-Type: multipart/mixed
            if (multipart.getCount() > 1) {
                message.setContent(multipart);
            }

            return message;
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new MailSendException(e.getMessage(), e);
        }
    }

    /**
     * 保存信息到已发送
     * @param message
     * @throws MessagingException
     */
    private void saveToOutbox(Message message) {
        try {
            // 标记已读
            message.setFlag(Flags.Flag.SEEN, true);
            Store store = getStore();
            Folder outBox = store.getFolder("已发送");
            outBox.open(Folder.READ_WRITE);
            outBox.appendMessages(new Message[]{message});
            outBox.close(true);
            store.close();
        } catch (MessagingException e) {
            throw new MailSendException(e.getMessage(), e);
        }
    }

    private Store getStore() throws MessagingException {
        if (cachedStore != null && cachedStore.isConnected()) {
            return cachedStore;
        }

        Session session = ((JavaMailSenderImpl)sender).getSession();
        IMAPStore store = (IMAPStore) session.getStore("imap");
        store.connect(imapMailProperties.getHost(), imapMailProperties.getPort() == null ? -1 : imapMailProperties.getPort(),  mailProperties.getUsername(), mailProperties.getPassword());

        // 163 Unsafe Login. Please contact kefu@188.com for help
        // 需要配置客户端参数 https://help.mail.163.com/faqDetail.do?code=d7a5dc8471cd0c0e8b4b8f4f8e49998b374173cfe9171305fa1ce630d7f67ac211b1978002df8b23
        Map clientParams = new HashMap();
        //带上IMAP ID信息，由key和value组成，例如name，version，vendor，support-email等。
        clientParams.put("name","sharp-mail");
        clientParams.put("version","1.0.0");
        clientParams.put("vendor","Rick");
        clientParams.put("support-email","jkxyx205@163.com");
        store.id(clientParams);

        cachedStore = store;
        return cachedStore;
    }

    private void closeStore() throws MessagingException {
        if (cachedStore != null && cachedStore.isConnected()) {
            cachedStore.close();
            cachedStore = null;
        }
    }

}
