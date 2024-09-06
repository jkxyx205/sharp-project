package com.rick.common.http;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/16/20 1:43 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public final class HttpServletRequestUtils {

    public static final String X_REQUESTED_WITH = "X-Requested-With";
    public static final String XML_HTTP_REQUEST = "XMLHttpRequest";

    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"};

    private HttpServletRequestUtils() {
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String accept = request.getHeader("accept");
        if (accept != null && accept.indexOf("application/json") != -1) {
            return true;
        }

        String xRequestedWith = request.getHeader(X_REQUESTED_WITH);
        if (xRequestedWith != null && xRequestedWith.indexOf(XML_HTTP_REQUEST) != -1) {
            return true;
        }

        String uri = request.getRequestURI();
        if (inStringIgnoreCase(uri, ".json" , ".xml")) {
            return true;
        }

        String ajax = request.getParameter("__ajax");
        return inStringIgnoreCase(ajax, "json", "xml");
    }

    public static boolean isNotAjaxRequest(HttpServletRequest request) {
        return !isAjaxRequest(request);
    }

    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : IP_HEADER_CANDIDATES) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public static Map<String, String> getParameterStringMap(HttpServletRequest request) {
        return getParameterStringMap(request, false);
    }

    public static Map<String, String> getParameterStringMap(HttpServletRequest request, boolean skipBlank) {
        Map<String, String> map = new HashMap<>(request.getParameterMap().size());
        addArrayValue(request, skipBlank, map, (array) -> StringUtils.join(array, ","));
        return map;
    }

    public static Map<String, Object> getParameterMap(HttpServletRequest request) {
        return getParameterMap(request, false);
    }

    /**
     * 将参数放到map中，数组value用,分隔
     * @param request
     * @param skipBlank 忽略空白
     * @return
     */
    public static Map<String, Object> getParameterMap(HttpServletRequest request, boolean skipBlank) {
        Map<String, Object> map = new HashMap<>(request.getParameterMap().size());
        addArrayValue(request, skipBlank, map, (array) -> array);
        return map;
    }

    public static Map<String, ?> getParameterMap(HttpServletRequest request, Map<String, ?> extendParams) {
        Map<String, Object> requestParams = HttpServletRequestUtils.getParameterMap(request);
        Map mergeParams = requestParams;

        if (MapUtils.isNotEmpty(extendParams)) {
            mergeParams = Maps.newHashMapWithExpectedSize(requestParams.size() + extendParams.size());
            mergeParams.putAll(requestParams);
            mergeParams.putAll(extendParams);
        }
        return mergeParams;
    }

    public static String getBodyString(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try (
                InputStream inputStream = request.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private static boolean inStringIgnoreCase(String str, String... strs) {
        if (str != null && strs != null) {
            for (String s : strs) {
                if (str.equalsIgnoreCase(s.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void addArrayValue(HttpServletRequest request, boolean skipBlank, Map map, Function<List<String>, Object> arraryFunction) {
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            String[] values = request.getParameterValues(name);

            if (Objects.nonNull(values)) {
                //多选 会在name后面加[]
                String property = name.replace("[]", "");
                if (values.length > 1 || name.endsWith("[]")) {
                    List<String> array = Lists.newArrayListWithExpectedSize(values.length);
                    for (String value : values) {
                        if (skipBlank && StringUtils.isBlank(value)) {
                            continue;
                        }
                        array.add(value);
                    }
                    map.put(property, arraryFunction.apply(array));
                } else {
                    String value = values[0];
                    if (skipBlank && StringUtils.isBlank(value)) {
                        continue;
                    }
                    map.put(property, value);
                }
            }
        }
    }

}
