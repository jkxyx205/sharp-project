package com.rick.db.repository.support.dialect;

import com.rick.db.plugin.page.PageModel;
import com.rick.db.repository.model.DatabaseType;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rick.db.repository.support.Constants.GROUP_DUMMY_TABLE_NAME;

/**
 * @author Rick.Xu
 *
 */
public abstract class AbstractDialect {

    /**
     * 合法名字：数字字母下划线
     */
    private static final String COLUMN_REGEX = "((?i)(to_char|NVL)?\\s*([(][^([(]|[)])]*[)])|[a-zA-Z0-9'[.]_[-]]+)(\\s*->\\s*'\\$.[a-zA-Z]+\\w*')?";

    private static final Pattern orderSQLPattern = Pattern.compile("order\\s*by\\s+(" + COLUMN_REGEX + ")(\\s*,\\s*" + COLUMN_REGEX + ")*(\\s+(desc|asc)(?!\\w+))?", Pattern.CASE_INSENSITIVE);

    public String formatSqlCount(String srcSql) {
		StringBuilder sb = new  StringBuilder();
		sb.append("SELECT COUNT(*) FROM (").append(removeOrders(srcSql)).append(") temp");
		return sb.toString();
	}

    public String wrapSordString(String sql, String sidx, String sord) {
        StringBuilder sb = new StringBuilder("SELECT * FROM (");
        sb.append(sql).append(") " + GROUP_DUMMY_TABLE_NAME);
        if (StringUtils.isNotBlank(sidx) && StringUtils.isNotBlank(sord)) {
            sb.append(" ORDER BY ").append(sidx).append(" ").append(sord);
            return sb.toString();
        } else {
            return sql;
        }
    }

    /**
     * 去除sql的orderBy子句。
     *
     * @param
     * @return
     */
    protected String removeOrders(String sqlString) {
        Matcher m = orderSQLPattern.matcher(sqlString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

	public abstract String pageSql(String sql, PageModel model);

	public abstract String contactString(String name);

	public abstract String escapeString();

    public abstract String getOrderBy(String tablePrefix, String column, Boolean asc, String[] sortableColumns);

    public abstract DatabaseType getType();

}
