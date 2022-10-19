package com.rick.common.http;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

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

    /**
     *      * 判断是否ajax请求.
     *      * 可以看到Ajax 请求多了个 x-requested-with ，可以利用它，
     *      * request.getHeader("x-requested-with"); 为 null，则为传统同步请求，为 XMLHttpRequest，则为Ajax 异步请求。
     *      *@paramrequest  HttpServletRequest
     *      *@return是否ajax请求.
     *     
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String xr = request.getHeader(X_REQUESTED_WITH);
        return (xr != null && XML_HTTP_REQUEST.equalsIgnoreCase(xr));
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

    public static Map<String, Object> getParameterMap(HttpServletRequest request) {
        return getParameterMap(request, false);
    }

    /**
     * 将参数放到map中，数组value用,分隔
     * @param request
     * @param skipBlink 忽略空白
     * @return
     */
    public static Map<String, Object> getParameterMap(HttpServletRequest request, boolean skipBlink) {
        Enumeration<String> en = request.getParameterNames();
        Map<String, Object> map = new HashMap<>(request.getParameterMap().size());

        while (en.hasMoreElements()) {
            String name = en.nextElement();
            String[] values = request.getParameterValues(name);

            //多选 会在name后面加[]
            name = name.replace("[]", "");

            if (Objects.nonNull(values)) {
                if (values.length > 1) {
                    List<String> array = Lists.newArrayListWithExpectedSize(values.length);
                    for (String value : values) {
                        if (skipBlink && StringUtils.isBlank(value)) {
                            continue;
                        }
                        array.add(value);
                    }

                    map.put(name, array);
                } else {
                    String value = values[0];
                    if (skipBlink && StringUtils.isBlank(value)) {
                        continue;
                    }
                    map.put(name, value);
                }
            }
        }

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

}
