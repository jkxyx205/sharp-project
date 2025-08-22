package com.rick.db.repository.support.dialect;

import com.rick.db.plugin.page.PageModel;
import com.rick.db.repository.model.DatabaseType;

/**
 * @author Rick.Xu
 * @date 2025/8/22 10:09
 */
public class SQLiteDialect extends AbstractDialect {

    @Override
    public String pageSql(String sql, PageModel model) {
        return null;
    }

    @Override
    public String contactString(String name) {
        return null;
    }

    @Override
    public String escapeString() {
        return null;
    }

    @Override
    public String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns) {
        return tablePrefix + column + " " + (asc ? "asc" : "desc");
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.SQLite;
    }
}
