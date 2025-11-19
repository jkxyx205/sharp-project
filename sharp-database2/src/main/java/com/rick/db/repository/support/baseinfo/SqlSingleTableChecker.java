package com.rick.db.repository.support.baseinfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL单表查询判断工具
 * 规则:
 * 1. 必须是SELECT语句
 * 2. 只能有一个表(FROM子句只有一个表名)
 * 3. 不能有JOIN
 * 4. 不能有子查询(包括WHERE、SELECT等任何位置的子查询)
 * 5. 不能有UNION
 * 6. 可以有WHERE条件(支持命名参数:param和占位符?)
 */
public class SqlSingleTableChecker {

    /**
     * 判断是否为单表查询(不允许子查询)
     */
    public static boolean isSingleTableQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return false;
        }

        sql = sql.trim();
        String upperSql = sql.toUpperCase();

        // 1. 必须是SELECT语句
        if (!upperSql.startsWith("SELECT")) {
            return false;
        }

        // 2. 不能包含子查询(检查括号中的SELECT)
        if (hasSubquery(sql)) {
            return false;
        }

        // 3. 不能包含JOIN
        if (hasJoin(upperSql)) {
            return false;
        }

        // 4. 不能包含UNION
        if (hasUnion(upperSql)) {
            return false;
        }

        // 5. FROM子句只能有一个表(不能有逗号分隔的多表)
        if (!hasSingleTable(upperSql)) {
            return false;
        }

        return true;
    }

    /**
     * 检查是否包含子查询
     */
    private static boolean hasSubquery(String sql) {
        // 检查括号中是否包含SELECT关键字
        int level = 0;
        StringBuilder insideBrackets = new StringBuilder();

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '(') {
                level++;
                if (level == 1) {
                    insideBrackets = new StringBuilder();
                }
            } else if (c == ')') {
                if (level == 1) {
                    // 检查括号内是否有SELECT
                    String content = insideBrackets.toString().trim().toUpperCase();
                    if (content.contains("SELECT")) {
                        return true;
                    }
                }
                level--;
            } else if (level > 0) {
                insideBrackets.append(c);
            }
        }

        return false;
    }

    /**
     * 检查是否包含JOIN
     */
    private static boolean hasJoin(String upperSql) {
        String[] joinKeywords = {
                " JOIN ", " INNER JOIN ", " LEFT JOIN ", " RIGHT JOIN ",
                " FULL JOIN ", " CROSS JOIN ", " OUTER JOIN ",
                " LEFT OUTER JOIN ", " RIGHT OUTER JOIN ", " FULL OUTER JOIN "
        };

        for (String join : joinKeywords) {
            if (upperSql.contains(join)) {
                return true;
            }
        }

        // 也检查JOIN在末尾的情况
        for (String join : joinKeywords) {
            if (upperSql.endsWith(join.trim())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 检查是否包含UNION
     */
    private static boolean hasUnion(String upperSql) {
        return upperSql.contains(" UNION ") ||
                upperSql.contains(" UNION ALL ") ||
                upperSql.endsWith(" UNION") ||
                upperSql.endsWith(" UNION ALL");
    }

    /**
     * 检查FROM子句是否只有一个表
     */
    private static boolean hasSingleTable(String upperSql) {
        // 提取FROM子句内容
        Pattern fromPattern = Pattern.compile(
                "FROM\\s+(.+)",
                Pattern.CASE_INSENSITIVE
        );

        Matcher matcher = fromPattern.matcher(upperSql);

        if (!matcher.find()) {
            return false; // 没有FROM子句
        }

        String fromClause = matcher.group(1).trim();

        // 移除FROM之后的WHERE/GROUP/ORDER/LIMIT/HAVING等子句
        fromClause = fromClause.split("\\s+WHERE\\s+", 2)[0];
        fromClause = fromClause.split("\\s+GROUP\\s+BY\\s+", 2)[0];
        fromClause = fromClause.split("\\s+ORDER\\s+BY\\s+", 2)[0];
        fromClause = fromClause.split("\\s+LIMIT\\s+", 2)[0];
        fromClause = fromClause.split("\\s+HAVING\\s+", 2)[0];
        fromClause = fromClause.split(";", 2)[0];
        fromClause = fromClause.trim();

        // 检查是否有逗号(多表)
        if (fromClause.contains(",")) {
            return false;
        }

        // 检查表名格式: tableName 或 tableName alias 或 tableName AS alias
        // 移除别名后应该只剩一个单词
        String[] parts = fromClause.split("\\s+");

        // 至少要有表名
        if (parts.length == 0) {
            return false;
        }

        // 最多只能是: tableName 或 tableName alias 或 tableName AS alias
        if (parts.length > 3) {
            return false;
        }

        // 如果有3部分,中间必须是AS
        if (parts.length == 3 && !parts[1].equalsIgnoreCase("AS")) {
            return false;
        }

        return true;
    }

    /**
     * 获取详细的分析结果
     */
    public static AnalysisResult analyze(String sql) {
        AnalysisResult result = new AnalysisResult();
        result.sql = sql;
        result.isSingleTable = true;

        if (sql == null || sql.trim().isEmpty()) {
            result.isSingleTable = false;
            result.reason = "SQL为空";
            return result;
        }

        String upperSql = sql.trim().toUpperCase();

        if (!upperSql.startsWith("SELECT")) {
            result.isSingleTable = false;
            result.reason = "不是SELECT语句";
            return result;
        }

        if (hasSubquery(sql)) {
            result.isSingleTable = false;
            result.reason = "包含子查询";
            return result;
        }

        if (hasJoin(upperSql)) {
            result.isSingleTable = false;
            result.reason = "包含JOIN操作";
            return result;
        }

        if (hasUnion(upperSql)) {
            result.isSingleTable = false;
            result.reason = "包含UNION操作";
            return result;
        }

        if (!hasSingleTable(upperSql)) {
            result.isSingleTable = false;
            result.reason = "FROM子句包含多个表";
            return result;
        }

        result.reason = "符合单表查询规则";
        return result;
    }

    /**
     * 分析结果类
     */
    public static class AnalysisResult {
        public String sql;
        public boolean isSingleTable;
        public String reason;

        @Override
        public String toString() {
            return String.format("[%s] %s - %s",
                    isSingleTable ? "✓" : "✗",
                    reason,
                    sql);
        }
    }

    // 测试方法
    public static void main(String[] args) {
        String[] testSqls = {
                // ✓ 单表查询
                "SELECT * FROM users",
                "SELECT id, name FROM users",
                "SELECT * FROM users WHERE age > 18",
                "SELECT * FROM users WHERE a = 5 AND b = :b AND c = ?",
                "select column1, column2 from tableName where a = 5 and b = :b and c = ?",
                "SELECT * FROM users u WHERE u.status = 'active'",
                "SELECT COUNT(*) FROM orders WHERE status = 'completed'",
                "SELECT * FROM products WHERE price > 100 ORDER BY price DESC",
                "SELECT name FROM users WHERE id IN (1,2,3)",
                "SELECT name FROM users WHERE id IN (:ids)",

                // ✗ 多表查询或包含子查询
                "SELECT u.name, o.total FROM users u JOIN orders o ON u.id = o.user_id",
                "SELECT * FROM users, orders WHERE users.id = orders.user_id",
                "SELECT * FROM products WHERE category_id IN (SELECT id FROM categories)",
                "SELECT * FROM users WHERE id = (SELECT user_id FROM orders WHERE id = 1)",
                "SELECT * FROM users UNION SELECT * FROM customers",
                "SELECT * FROM orders LEFT JOIN users ON orders.user_id = users.id",
                "SELECT (SELECT COUNT(*) FROM orders) as cnt FROM users",
                "SELECT * FROM users WHERE EXISTS (SELECT 1 FROM orders WHERE user_id = users.id)",
                "UPDATE users SET name = 'test'",
                "SELECT COUNT(*) FROM (SELECT id, code, description FROM mm_material WHERE UPPER(code) like CONCAT('%',UPPER(:code),'%') escape '\\'  AND mm_material.is_deleted = FALSE) temp",
                ""
        };

        System.out.println("========== SQL单表查询分析 ==========\n");

        for (String sql : testSqls) {
            AnalysisResult result = analyze(sql);
            System.out.println(result);
        }

        System.out.println("\n========== 简单判断示例 ==========\n");

        String sql1 = "SELECT * FROM users WHERE a = 5 AND b = :b AND c = ?";
        System.out.println("SQL: " + sql1);
        System.out.println("是否单表查询: " + isSingleTableQuery(sql1));

        String sql2 = "SELECT * FROM users WHERE id IN (SELECT user_id FROM orders)";
        System.out.println("\nSQL: " + sql2);
        System.out.println("是否单表查询: " + isSingleTableQuery(sql2));
    }
}