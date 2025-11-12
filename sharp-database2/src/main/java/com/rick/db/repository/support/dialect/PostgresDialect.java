package com.rick.db.repository.support.dialect;

import com.rick.db.plugin.page.PageModel;
import com.rick.db.repository.model.DatabaseType;
import org.apache.commons.lang3.StringUtils;

import static com.rick.db.repository.support.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 * @author Rick.Xu
 * @date 2025/6/30 14:31
 */
public class PostgresDialect extends AbstractDialect {

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
            sb.append(" LIMIT ").append(model.getSize()).append(" OFFSET ").append((model.getPage() - 1) * model.getSize());

        return sb.toString();
    }

    @Override
    public String contactString(String name) {
        return " CONCAT('%',UPPER(:" + name + "),'%')";
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
        return DatabaseType.PostgreSQL;
    }

    @Override
    public String summaryFun(String column) {
        return "CAST(SUM("+column+") AS NUMERIC(20,3))";
    }
}
