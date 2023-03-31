package com.rick.common.util;

import com.google.common.base.CaseFormat;
import com.rick.common.constant.Constants;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Rick
 * @createdAt 2021-06-03 17:27:00
 */
public final class StringUtils {

    /**
     * 生成图片要显示的名字
     * @param name
     * @return
     */
    public static final String generateImgName(String name) {
        int nameLen = name.length();

        String nameWritten;
        //如果用户输入的姓名少于等于2个字符，不用截取
        if (nameLen <= 2) {
            nameWritten = name;
        } else {
            //如果用户输入的姓名大于等于3个字符，截取后面一位  可根据需求改长度
            String first = name.substring(0, 1);
            if (isChinese(first)) {
                //截取倒数两位汉字
                nameWritten = name.substring(nameLen - 2);
            } else {
                //截取前面的两个英文字母
                nameWritten = name.substring(0, 2).toUpperCase();
            }
        }

        return nameWritten;
    }

    /**
     * 判断字符串是否为中文
     *
     * @param str
     * @return
     */
    public static final boolean isChinese(String str) {
        String regEx = "[\\u4e00-\\u9fa5]+";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    /**
     * 从html中提取纯文本
     */
    public static String getContent(String htmlStr) {
        String textStr = "";
        Pattern p_script;
        java.util.regex.Matcher m_script;
        Pattern p_style;
        java.util.regex.Matcher m_style;
        Pattern p_html;
        java.util.regex.Matcher m_html;
        try {
            // 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script>
            String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
            // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
            String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
            // 定义HTML标签的正则表达式
            String regEx_html = "<[^>]+>";
            p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
            m_script = p_script.matcher(htmlStr);
            // 过滤script标签
            htmlStr = m_script.replaceAll("");
            p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
            m_style = p_style.matcher(htmlStr);
            // 过滤style标签
            htmlStr = m_style.replaceAll("");
            p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
            m_html = p_html.matcher(htmlStr);
            // 过滤html标签
            htmlStr = m_html.replaceAll("");
            textStr = htmlStr;
        } catch (Exception e) {
            System.err.println("Html2Text: " + e.getMessage());
        }
        //剔除空格行
        textStr = textStr.replaceAll("(\\\\r|\\\\n)", "");
        textStr = textStr.replace("'", "\\'");
        return textStr;
    }

    /**
     *
     * @param url \\a/b//cc
     * @return /a/b/c
     */
    public static String formatURLSeparator(String url) {
        if (url == null) {
            return null;
        }

        return url.replaceAll("(\\\\+)|([/]{2,})", "/");
    }

    /**
     * 根据“文件名的后缀”获取文件内容类型（而非根据File.getContentType()读取的文件类型）
     * @param fileName 带验证的文件名
     * @return 返回文件类型
     */
    public static String getContentType(String fileName) {
        String contentType = "application/octet-stream";
        if (fileName.lastIndexOf(".") < 0)
            return contentType;

        fileName = fileName.toLowerCase();
        fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (fileName.equals("html") || fileName.equals("htm") || fileName.equals("shtml")) {
            contentType = "text/html";
        } else if (fileName.equals("apk")) {
            contentType = "application/vnd.android.package-archive";
        } else if (fileName.equals("sis")) {
            contentType = "application/vnd.symbian.install";
        } else if (fileName.equals("sisx")) {
            contentType = "application/vnd.symbian.install";
        } else if (fileName.equals("exe")) {
            contentType = "application/x-msdownload";
        } else if (fileName.equals("msi")) {
            contentType = "application/x-msdownload";
        } else if (fileName.equals("css")) {
            contentType = "text/css";
        } else if (fileName.equals("xml")) {
            contentType = "text/xml";
        } else if (fileName.equals("gif")) {
            contentType = "image/gif";
        } else if (fileName.equals("jpeg") || fileName.equals("jpg")) {
            contentType = "image/jpeg";
        } else if (fileName.equals("js")) {
            contentType = "application/x-javascript";
        } else if (fileName.equals("atom")) {
            contentType = "application/atom+xml";
        } else if (fileName.equals("rss")) {
            contentType = "application/rss+xml";
        } else if (fileName.equals("mml")) {
            contentType = "text/mathml";
        } else if (fileName.equals("txt")) {
            contentType = "text/plain";
        } else if (fileName.equals("jad")) {
            contentType = "text/vnd.sun.j2me.app-descriptor";
        } else if (fileName.equals("wml")) {
            contentType = "text/vnd.wap.wml";
        } else if (fileName.equals("htc")) {
            contentType = "text/x-component";
        } else if (fileName.equals("png")) {
            contentType = "image/png";
        } else if (fileName.equals("tif") || fileName.equals("tiff")) {
            contentType = "image/tiff";
        } else if (fileName.equals("wbmp")) {
            contentType = "image/vnd.wap.wbmp";
        } else if (fileName.equals("ico")) {
            contentType = "image/x-icon";
        } else if (fileName.equals("jng")) {
            contentType = "image/x-jng";
        } else if (fileName.equals("bmp")) {
            contentType = "image/x-ms-bmp";
        } else if (fileName.equals("svg")) {
            contentType = "image/svg+xml";
        } else if (fileName.equals("jar") || fileName.equals("var")
                || fileName.equals("ear")) {
            contentType = "application/java-archive";
        } else if (fileName.equals("doc")) {
            contentType = "application/msword";
        } else if (fileName.equals("pdf")) {
            contentType = "application/pdf";
        } else if (fileName.equals("rtf")) {
            contentType = "application/rtf";
        } else if (fileName.equals("xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (fileName.equals("ppt")) {
            contentType = "application/vnd.ms-powerpoint";
        } else if (fileName.equals("7z")) {
            contentType = "application/x-7z-compressed";
        } else if (fileName.equals("rar")) {
            contentType = "application/x-rar-compressed";
        } else if (fileName.equals("swf")) {
            contentType = "application/x-shockwave-flash";
        } else if (fileName.equals("rpm")) {
            contentType = "application/x-redhat-package-manager";
        } else if (fileName.equals("der") || fileName.equals("pem")
                || fileName.equals("crt")) {
            contentType = "application/x-x509-ca-cert";
        } else if (fileName.equals("xhtml")) {
            contentType = "application/xhtml+xml";
        } else if (fileName.equals("zip")) {
            contentType = "application/zip";
        } else if (fileName.equals("mid") || fileName.equals("midi")
                || fileName.equals("kar")) {
            contentType = "audio/midi";
        } else if (fileName.equals("mp3")) {
            contentType = "audio/mpeg";
        } else if (fileName.equals("ogg")) {
            contentType = "audio/ogg";
        } else if (fileName.equals("m4a")) {
            contentType = "audio/x-m4a";
        } else if (fileName.equals("ra")) {
            contentType = "audio/x-realaudio";
        } else if (fileName.equals("3gpp")
                || fileName.equals("3gp")) {
            contentType = "video/3gpp";
        } else if (fileName.equals("mp4")) {
            contentType = "video/mp4";
        } else if (fileName.equals("mpeg")
                || fileName.equals("mpg")) {
            contentType = "video/mpeg";
        } else if (fileName.equals("mov")) {
            contentType = "video/quicktime";
        } else if (fileName.equals("flv")) {
            contentType = "video/x-flv";
        } else if (fileName.equals("m4v")) {
            contentType = "video/x-m4v";
        } else if (fileName.equals("mng")) {
            contentType = "video/x-mng";
        } else if (fileName.equals("asx") || fileName.equals("asf")) {
            contentType = "video/x-ms-asf";
        } else if (fileName.equals("wmv")) {
            contentType = "video/x-ms-wmv";
        } else if (fileName.equals("avi")) {
            contentType = "video/x-msvideo";
        }
        return contentType;
    }

    public static String getFilenameExtension(@Nullable String path) {
        if (path == null) {
            return null;
        } else {
            int extIndex = path.lastIndexOf(46);
            if (extIndex == -1) {
                return null;
            } else {
                int folderIndex = path.lastIndexOf("/");
                return folderIndex > extIndex ? null : path.substring(extIndex + 1);
            }
        }
    }

    public static String stripFilenameExtension(String path) {
        int extIndex = path.lastIndexOf(46);
        if (extIndex == -1) {
            return path;
        } else {
            int folderIndex = path.lastIndexOf("/");
            return folderIndex > extIndex ? path : path.substring(0, extIndex);
        }
    }

    public static String getFilename(String path) {
        if (path == null) {
            return null;
        } else {
            int separatorIndex = path.lastIndexOf("/");
            return separatorIndex != -1 ? path.substring(separatorIndex + 1) : path;
        }
    }

    public static String fullName(String name, String extension) {
        if (org.apache.commons.lang3.StringUtils.isBlank(extension)) {
            return name;
        }

        return name +  "." + extension;
    }

    public static boolean isImageType(String extension, String contentType) {
        if (isImageExtension(extension)) {
            return true;
        }

        if (Objects.nonNull(contentType) && contentType.startsWith("image")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否是合法的文件类型
     * @param extension
     * @return
     */
    public static boolean isImageExtension(String extension) {
        for (String s : Constants.IMAGE_TYPE) {
            if (s.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

    // found at https://www.geeksforgeeks.org/convert-snake-case-string-to-camel-case-in-java/#:~:text=replaceFirst()%20method%20to%20convert,next%20letter%20of%20the%20underscore.
    // Function to convert the string
    // from snake case to camel case
    public static String stringToCamel(String str) {
        if (str.contains("_")) {
            // Capitalize first letter of string
            str = str.toLowerCase();
            str = str.substring(0, 1)
                    + str.substring(1);

            // Convert to StringBuilder
            StringBuilder builder
                    = new StringBuilder(str);

            // Traverse the string character by
            // character and remove underscore
            // and capitalize next letter
            for (int i = 0; i < builder.length(); i++) {

                // Check char is underscore
                if (builder.charAt(i) == '_') {

                    builder.deleteCharAt(i);
                    builder.replace(
                            i, i + 1,
                            String.valueOf(
                                    Character.toUpperCase(
                                            builder.charAt(i))));
                }
            }

            // Return in String type
            return builder.toString();
        } else if(str.matches("[A-Z]+")) {
            return str.toLowerCase();
        } else if (str.charAt(0)>= 'A' && str.charAt(0) <= 'Z'){
            return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, str);
        }

        // Return string
        return str;
    }

    public static String camelToSnake(final String camelStr) {
        String ret = camelStr.replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2").replaceAll("([a-z])([A-Z])", "$1_$2");
        return ret.toLowerCase();
    }

}
