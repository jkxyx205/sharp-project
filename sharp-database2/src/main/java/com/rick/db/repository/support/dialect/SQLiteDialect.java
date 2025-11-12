package com.rick.db.repository.support.dialect;

import com.rick.db.plugin.page.PageModel;
import com.rick.db.repository.model.DatabaseType;
import org.apache.commons.lang3.StringUtils;

import static com.rick.db.repository.support.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 * @author Rick.Xu
 * @date 2025/8/22 10:09
 */
public class SQLiteDialect extends AbstractDialect {

    @Override
    public String pageSql(String sql, PageModel model) {
        StringBuilder sb = new  StringBuilder();
        sb.append("SELECT * FROM ")
                .append("(").append(sql).append(") ").append(GROUP_DUMMY_TABLE_NAME);

        if (model != null && (StringUtils.isNotBlank(model.getSidx()) && StringUtils
                .isNotBlank(model.getSord()))) {
            sb.append(" ORDER BY ").append(model.getSidx()).append(" ")
                    .append(model.getSord());
        }

        if (model != null)
            sb.append(" LIMIT ").append((model.getPage()-1) *  model.getSize()).append(",").append(model.getSize());

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
        return DatabaseType.SQLite;
    }

    @Override
    public String summaryFun(String column) {
        return "CAST(SUM("+column+") AS NUMERIC)";
    }
}
