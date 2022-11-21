package com.rick.mail.util;

import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Rick
 * @createdAt 2022-11-17 00:28:00
 */
@UtilityClass
public class MailUtils {

    public List<String> downloadAttachments(Message message, String downloadFolder) throws IOException, MessagingException {
        List<String> downloadedAttachments = new ArrayList<>();
        if (message.getContent() instanceof Multipart) {
            Multipart multiPart = (Multipart) message.getContent();
            int numberOfParts = multiPart.getCount();
            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    String fileName = MimeUtility.decodeText(MimeUtility.unfold(part.getFileName()));
                    part.saveFile(downloadFolder + File.separator + fileName);
//                part.writeTo();
//                part.getInputStream();
                    downloadedAttachments.add(fileName);
                }
            }
        }
        return downloadedAttachments;
    }

    /**
     * 判断邮件中是否包含附件
     * @param part 邮件内容
     * @return 邮件中存在附件返回true，不存在返回false
     * @throws MessagingException
     * @throws IOException
     */
    public static boolean hasAttachment(Part part) throws MessagingException, IOException {
        if (part.getContent() instanceof Multipart) {
            Multipart multiPart = (Multipart) part.getContent();
            int numberOfParts = multiPart.getCount();
            for (int partCount = 0; partCount < numberOfParts; partCount++) {
                MimeBodyPart bodyPart = (MimeBodyPart) multiPart.getBodyPart(partCount);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 获取正文内容
     * @param object
     * @return
     * @throws IOException
     * @throws MessagingException
     */
    public static String contentText(Object object) throws IOException, MessagingException {
        if (Objects.isNull(object)) {
            return "";
        }

        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof MimeMultipart) {
            MimeMultipart mimeMultipart = (MimeMultipart) object;
            return getTextFromMimeMultipart(mimeMultipart);
        }

        return object.toString();
    }

    /**
     * Extracts the text content of a multipart email message
     */
    private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = "";
        int partCount = mimeMultipart.getCount();
        for (int i = 0; i < partCount; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (Part.ATTACHMENT.equals(bodyPart.getDisposition())) {
                break;
            }

            Object content = bodyPart.getContent();
            if (bodyPart.isMimeType("text/plain")) {
                result = result + content;
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) content;
                result = result + Jsoup.parse(html).text();
            } else if (content instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) content);
            }
        }

        result = MimeUtility.unfold(result);
        return result;
    }
}
