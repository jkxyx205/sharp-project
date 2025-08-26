package com.rick.db.repository.support.dialect;

import com.rick.db.plugin.page.PageModel;
import com.rick.db.repository.model.DatabaseType;
import org.apache.commons.lang3.StringUtils;

import static com.rick.db.repository.support.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 * @author Rick.Xu
 * @date 2025/8/26 10:14
 */
public class SQLServer2012Dialect extends AbstractDialect {

    @Override
    public String pageSql(String sql, PageModel model) {
        StringBuilder sb = new StringBuilder();

        // SQL Server 使用 OFFSET...FETCH 进行分页
        sb.append("SELECT * FROM ")
                .append("(").append(sql).append(") ").append(GROUP_DUMMY_TABLE_NAME);

        if (model != null && (StringUtils.isNotBlank(model.getSidx()) && StringUtils
                .isNotBlank(model.getSord()))) {
            sb.append(" ORDER BY ").append(model.getSidx()).append(" ")
                    .append(model.getSord());
        } else {
            // SQL Server 的 OFFSET...FETCH 需要 ORDER BY 子句
            sb.append(" ORDER BY (SELECT NULL)");
        }

        if (model != null) {
            // SQL Server 2012+ 分页语法
            sb.append(" OFFSET ").append((model.getPage()-1) * model.getSize())
                    .append(" ROWS FETCH NEXT ").append(model.getSize()).append(" ROWS ONLY");
        }

        return sb.toString();
    }

    @Override
    public String contactString(String name) {
        // SQL Server 使用 + 进行字符串连接，而不是 CONCAT 函数
        return " '%' + UPPER(@" + name + ") + '%'";
    }

    @Override
    public String escapeString() {
        // SQL Server 使用单引号转义
        return "escape ''''";
    }

    @Override
    public String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns) {
        return tablePrefix + column + " " + (asc ? "asc" : "desc");
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.SQLServer2012;
    }
}
