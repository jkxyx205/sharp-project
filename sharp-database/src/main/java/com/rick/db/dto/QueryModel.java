package com.rick.db.dto;

import lombok.Data;

import java.util.Map;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/16/20 2:35 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Data
public class QueryModel {

    private PageModel pageModel;

    private Map<String, ?> params;

    private QueryModel() {}

    public static QueryModel of(Map<String, ?> requestMap) {
        QueryModel queryModel = new QueryModel();
        queryModel.setParams(requestMap);

        PageModel pageModel = new PageModel();

        Object page = requestMap.get(PageModel.PARAM_PAGE);
        Object size = requestMap.get(PageModel.PARAM_SIZE);
        Object sidx = requestMap.get(PageModel.PARAM_SIDX);
        Object sord = requestMap.get(PageModel.PARAM_SORD);

        if (Objects.nonNull(page)) {
            pageModel.setPage(Integer.valueOf(String.valueOf(page)));
        }

        if (Objects.nonNull(size)) {
            pageModel.setSize(Integer.valueOf(String.valueOf(size)));
        }

        if (Objects.nonNull(sidx)) {
            pageModel.setSidx((String) sidx);
        }

        if (Objects.nonNull(sord)) {
            pageModel.setSord((String) sord);
        }

        queryModel.setPageModel(pageModel);
        return queryModel;
    }

    public static QueryModel of(PageModel pageModel) {
        QueryModel queryModel = new QueryModel();
        queryModel.setPageModel(pageModel);
        queryModel.setParams(pageModel.toMap());
        return queryModel;
    }

}
