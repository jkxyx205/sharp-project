package com.rick.admin.common.layout;

import com.rick.common.http.HttpServletRequestUtils;
import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rick.Xu
 * @date 2025/6/18 08:05
 */
@UtilityClass
public class LayoutUtils {

    public String page(HttpServletRequest request, String page) {
        return page(request, page, "content");
    }

    public String page(HttpServletRequest request, String page, String fragment) {
        return HttpServletRequestUtils.isAjaxRequest(request) ? page +" :: "+fragment+"" : page;
    }

}
