package com.rick.common.util;

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

}
