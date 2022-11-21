package com.rick.mail.core;

import com.rick.mail.core.mail.Email;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Rick
 * @createdAt 2022-11-16 21:57:00
 */
public interface MailHandler {
    /**
     * 发送邮件
     * @param email
     * @return
     */
    MimeMessage send(Email email);

    /**
     * 发送邮件并保存到已发送
     * @param email
     * @return
     */
    MimeMessage sendAndSaveToOutbox(Email email);

    MimeMessage send(MimeMessage message);

    MimeMessage sendAndSaveToOutbox(MimeMessage message);

    /**
     * 根据Message ID 获取邮件
     * @param folderName
     * @param messageId
     * @return
     * @throws MessagingException
     */
    Optional<MimeMessage> searchByMessageId(String folderName, String messageId) throws MessagingException;

    Optional<MimeMessage> searchByMessageId(Folder folder, String messageId) throws MessagingException;

    /**
     * 邮箱文件夹列表
     * @return
     * @throws MessagingException
     */
    Folder[] listFolders() throws MessagingException;

    /**
     * 未读邮件列表
     * @param consumer
     * @throws MessagingException
     */
    void listUnreadMessage(Consumer<MimeMessage> consumer) throws MessagingException;

    /**
     * 文件夹所有邮件
     * @param folderName
     * @param consumer true 表示终止遍历
     * @throws MessagingException
     */
    void listMessages(String folderName, Function<MimeMessage, Boolean> consumer) throws MessagingException;

    void listMessages(Folder folder, Function<MimeMessage, Boolean> consumer) throws MessagingException;

}
