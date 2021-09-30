package com.rick.common.http;


import com.rick.common.util.StringUtils;
import org.apache.commons.codec.binary.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/16/20 1:43 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public final class HttpServletResponseUtils {

    private static final String CHARSET_UTF_8 = StandardCharsets.UTF_8.name();

    //判断是否是ie浏览器
    private static String[] IEBrowserSignals = {"MSIE", "Trident", "Edge"};

    private static final String ATTACHMENT = "attachment";

    private static final String INLINE = "inline";

    private HttpServletResponseUtils() {
    }

    public static OutputStream getOutputStreamAsAttachment(HttpServletRequest request, HttpServletResponse response, String fileName) throws IOException {
        return getOutputStream(request, response, fileName, ATTACHMENT);
    }

    public static OutputStream getOutputStreamAsView(HttpServletRequest request, HttpServletResponse response, String fileName) throws IOException {
        return getOutputStream(request, response, fileName, INLINE);
    }

    public static OutputStream getOutputStream(HttpServletRequest request, HttpServletResponse response, String fileName, String type) throws IOException {
        String _fileName = fileName.replaceAll("[/:*?\"<>[|]]", "");

        String browserType = request.getHeader("User-Agent").toLowerCase();

        if(browserType.indexOf("firefox") > -1) { //FF
            _fileName = "=?"+ StandardCharsets.UTF_8+"?B?"+(new String(Base64.encodeBase64(_fileName.getBytes(StandardCharsets.UTF_8))))+"?=";
        } else {
            if(fileName.matches(".*[^\\x00-\\xff]+.*")) { // 是否包含中文
                if(isMSBrowser(request)) { //IE Edge
                    _fileName = java.net.URLEncoder.encode(_fileName,StandardCharsets.UTF_8.name());
                } else  { //其他
                    _fileName = new String(_fileName.getBytes(StandardCharsets.UTF_8), "ISO-8859-1");
                }
            }
        }

        response.reset();// 清空输出流
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Content-disposition", ""+type+"; filename="+_fileName+"");// 设定输出文件头
        response.setContentType(StringUtils.getContentType(_fileName));// 定义输出类型
        OutputStream os = response.getOutputStream(); // 取得输出流Files
        return os;
    }

    public static void write(HttpServletResponse response, String contentType, String value) {
        response.setContentType(contentType);
        try {
            PrintWriter pw = response.getWriter();
            pw.write(value);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isMSBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        for (String signal : IEBrowserSignals) {
            if (userAgent.contains(signal))
                return true;
        }
        return false;
    }
}
