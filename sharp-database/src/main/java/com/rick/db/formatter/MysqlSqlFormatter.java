package com.rick.db.formatter;

import com.rick.db.dto.PageModel;
import org.apache.commons.lang3.StringUtils;

import static com.rick.db.config.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/12/19 7:24 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class MysqlSqlFormatter extends AbstractSqlFormatter {

    @Override
    public  String pageSql(String sql, PageModel model) {
        StringBuilder sb = new  StringBuilder();
        sb.append("SELECT * FROM ")
                .append("(").append(sql).append(") ").append(GROUP_DUMMY_TABLE_NAME);

        if (model != null && (StringUtils.isNotBlank(model.getSidx()) && StringUtils
                .isNotBlank(model.getSord()))) {
            sb.append(" ORDER BY ").append(model.getSidx()).append(" ")
                    .append(model.getSord());
        }

        if (model != null)
            sb.append(" limit ").append((model.getPage()-1) *  model.getSize()).append(",").append(model.getSize());

        return sb.toString();
    }

    @Override
    public String contactString(String name) {
        return " CONCAT('%',UPPER(:" + name + "),'%')";
    }

    @Override
    public String escapeString() {
        return "escape '\\\\'";
    }
}
