package com.rick.db.util;

import java.util.HashMap;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/13/19 1:41 AM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public final class PaginationHelper {
    /**
     * 当分页较多的时候，希望显示部分分页
     * @param total 总页数
     * @param displayPage 希望ui显示页数
     * @param activePage 当前页
     * @return startPage endPage
     */
    public static Map<String, Long> limitPages(long total, long displayPage, long activePage) {
        Map<String, Long> pages = new HashMap<>(2);

        long leftOffset = (displayPage - 1) / 2;
        long leftPage = activePage - 1;

        long rightOffset = displayPage - leftOffset - 1;
        long rightPage = total - activePage;

        long startPage = 1;

        if (rightPage < rightOffset) {
            leftOffset = leftOffset + rightOffset - rightPage;
        }

        if (leftPage > leftOffset) {
            startPage = activePage - leftOffset;
        }

        long endPage = startPage + displayPage - 1;

        if (endPage > total) {
            endPage = total;
        }

        pages.put("startPage", startPage);
        pages.put("endPage", endPage);

        return pages;
    }
}
