package com.rick.db.repository.support.dialect;

import com.rick.db.plugin.page.PageModel;
import com.rick.db.repository.model.DatabaseType;
import org.apache.commons.lang3.StringUtils;

import static com.rick.db.repository.support.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/12/19 7:24 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class Oracle10gDialect extends AbstractDialect {

    @Override
    public String pageSql(String sql, PageModel model) {
        StringBuilder sb = new  StringBuilder();

        int startIndex = 0;
        int endIndex = Integer.MAX_VALUE;

        if(model != null) {
            startIndex = (model.getPage()-1)*model.getSize();
            endIndex = startIndex + model.getSize();
        }
        //
        sb.append("SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (SELECT * FROM ")
                .append("(").append(sql).append(") ").append(GROUP_DUMMY_TABLE_NAME);

        if (model != null
                && (StringUtils.isNotBlank(model.getSidx()) && StringUtils
                .isNotBlank(model.getSord()))) {
            sb.append(" ORDER BY ").append(model.getSidx()).append(" ")
                    .append(model.getSord());
        }

        sb.append(") A WHERE ROWNUM <=").append(endIndex).append(") WHERE RN > ").append(startIndex);
        return sb.toString();
    }

    @Override
    public String contactString(String name) {
        return "'%'||UPPER(:" + name + ")||'%'";
    }

    @Override
    public String escapeString() {
        return "escape '\\'";
    }

    @Override
    public String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns) {
        return tablePrefix + column + " " + (asc ? "asc" : "desc");
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.Oracle10g;
    }

    @Override
    public String summaryFun(String column) {
        return "CAST(SUM("+column+") AS NUMBER(20,3))";
    }

    @Override
    public String removeOrders(String sqlString) {
        if (sqlString.matches("(?is)(.*)LISTAGG(.*)")) {
            return sqlString;
        }

        return super.removeOrders(sqlString);
    }
}
