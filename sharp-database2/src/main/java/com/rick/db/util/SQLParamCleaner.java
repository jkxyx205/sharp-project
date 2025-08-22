package com.rick.db.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL片段清理工具
 * 功能：
 * 1. 自动清理空参数对应的字段/条件
 * 2. 去除多余的逗号、AND、OR操作符
 * 3. 删除括号内的空内容
 * 4. 支持完整SQL和SQL片段
 */
public class SQLParamCleaner {

    // 参数匹配模式（支持 :param 和 #{param} 格式）
    private static final Pattern PARAM_PATTERN = Pattern.compile(":(\\w+)|#\\{(\\w+)\\}");

    /**
     * 清理SQL，移除空参数和不存在参数对应的部分
     *
     * @param sql 原始SQL
     * @param params 参数Map，null、空字符串、空集合的参数以及不存在的参数将被清理
     * @return 清理后的SQL
     */
    public static String cleanSQL(String sql, Map<String, Object> params) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // 获取所有需要清理的参数（空参数 + 不存在的参数）
        Set<String> paramsToRemove = getParamsToRemove(sql, params);

        String result = sql;

        // 判断SQL类型并处理
        if (isUpdateSQL(sql)) {
            result = cleanUpdateSQL(result, paramsToRemove);
        } else if (isSelectSQL(sql)) {
            result = cleanSelectSQL(result, paramsToRemove);
        } else if (isDeleteSQL(sql)) {
            result = cleanDeleteSQL(result, paramsToRemove);
        } else if (isInsertSQL(sql)) {
            result = cleanInsertSQL(result, paramsToRemove);
        } else {
            // 处理SQL片段
            result = cleanSQLFragment(result, paramsToRemove);
        }

        // 最终清理
        result = finalClean(result);

        return result.trim();
    }

    /**
     * 清理UPDATE SQL
     */
    private static String cleanUpdateSQL(String sql, Set<String> paramsToRemove) {
        // 分解UPDATE SQL的各个部分
        String[] parts = splitUpdateSQL(sql);
        String updatePart = parts[0]; // UPDATE table SET
        String setPart = parts[1];    // field1 = :param1, field2 = :param2
        String wherePart = parts[2];  // WHERE conditions

        // 清理SET部分
        if (setPart != null && !setPart.trim().isEmpty()) {
            setPart = cleanSetClause(setPart, paramsToRemove);
        }

        // 清理WHERE部分
        if (wherePart != null && !wherePart.trim().isEmpty()) {
            wherePart = cleanWhereClause(wherePart, paramsToRemove);
        }

        // 重组SQL
        StringBuilder result = new StringBuilder(updatePart);
        if (setPart != null && !setPart.trim().isEmpty()) {
            result.append(" ").append(setPart);
        }
        if (wherePart != null && !wherePart.trim().isEmpty()) {
            result.append(" ").append(wherePart);
        }

        return result.toString();
    }

    /**
     * 清理SELECT SQL
     */
    private static String cleanSelectSQL(String sql, Set<String> paramsToRemove) {
        // 主要处理WHERE子句
        return cleanWhereInSQL(sql, paramsToRemove);
    }

    /**
     * 清理DELETE SQL
     */
    private static String cleanDeleteSQL(String sql, Set<String> paramsToRemove) {
        // 主要处理WHERE子句
        return cleanWhereInSQL(sql, paramsToRemove);
    }

    /**
     * 清理INSERT SQL
     */
    private static String cleanInsertSQL(String sql, Set<String> paramsToRemove) {
        // INSERT语句的清理逻辑
        Pattern insertPattern = Pattern.compile("(INSERT\\s+INTO\\s+\\w+)\\s*\\(([^)]+)\\)\\s*VALUES\\s*\\(([^)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = insertPattern.matcher(sql);

        if (matcher.find()) {
            String tablePart = matcher.group(1);
            String columnsPart = matcher.group(2);
            String valuesPart = matcher.group(3);

            String[] columns = columnsPart.split(",");
            String[] values = valuesPart.split(",");

            List<String> validColumns = new ArrayList<>();
            List<String> validValues = new ArrayList<>();

            for (int i = 0; i < Math.min(columns.length, values.length); i++) {
                String column = columns[i].trim();
                String value = values[i].trim();

                // 检查值是否包含需要移除的参数
                Set<String> paramsInValue = extractParams(value);
                boolean hasParamToRemove = paramsInValue.stream().anyMatch(paramsToRemove::contains);

                if (!hasParamToRemove) {
                    validColumns.add(column);
                    validValues.add(value);
                }
            }

            if (!validColumns.isEmpty()) {
                return tablePart + " (" + String.join(", ", validColumns) + ") VALUES (" + String.join(", ", validValues) + ")";
            }
        }

        return sql;
    }

    /**
     * 清理SQL片段
     */
    private static String cleanSQLFragment(String fragment, Set<String> paramsToRemove) {
        String result = fragment;

        // 如果看起来像SET子句
        if (fragment.trim().toLowerCase().startsWith("set") ||
                fragment.contains("=") && fragment.contains(",")) {
            result = cleanSetClause(result, paramsToRemove);
        }
        // 如果看起来像WHERE子句
        else if (fragment.trim().toLowerCase().startsWith("where") ||
                fragment.contains("AND") || fragment.contains("OR")) {
            result = cleanWhereClause(result, paramsToRemove);
        }
        // 其他情况，通用清理
        else {
            result = cleanGenericClause(result, paramsToRemove);
        }

        return result;
    }

    /**
     * 清理SET子句 - 智能处理逗号
     */
    private static String cleanSetClause(String setClause, Set<String> paramsToRemove) {
        String clause = setClause.trim();

        // 移除SET关键字进行处理
        boolean hasSetKeyword = clause.toLowerCase().startsWith("set");
        if (hasSetKeyword) {
            clause = clause.substring(3).trim();
        }

        // 使用智能方式处理赋值语句
        clause = removeAssignmentsWithCommas(clause, paramsToRemove);

        String result = clause.trim();
        if (hasSetKeyword && !result.isEmpty()) {
            result = "SET " + result;
        }

        return result;
    }

    /**
     * 智能移除赋值语句及其逗号
     */
    private static String removeAssignmentsWithCommas(String setContent, Set<String> paramsToRemove) {
        String result = setContent;

        for (String paramToRemove : paramsToRemove) {
            String[] assignmentPatterns = {
                    "\\s*\\w+\\s*=\\s*:" + paramToRemove + "(?!\\w)",
                    "\\s*\\w+\\s*=\\s*#\\{" + paramToRemove + "\\}"
            };

            for (String assignmentPattern : assignmentPatterns) {
                Pattern pattern = Pattern.compile(assignmentPattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(result);

                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();

                    // 检查这个赋值是否在开头位置
                    boolean isFirstAssignment = isAssignmentAtStart(result, start);

                    if (isFirstAssignment) {
                        // 如果是第一个赋值，移除后面的逗号
                        result = removeAssignmentAndFollowingComma(result, start, end);
                    } else {
                        // 如果不是第一个赋值，移除前面的逗号
                        result = removeAssignmentAndPrecedingComma(result, start, end);
                    }

                    // 重新匹配
                    matcher = pattern.matcher(result);
                }
            }
        }

        return result;
    }

    /**
     * 判断赋值是否在开头位置
     */
    private static boolean isAssignmentAtStart(String setContent, int assignmentStart) {
        String beforeAssignment = setContent.substring(0, assignmentStart);
        return beforeAssignment.trim().isEmpty();
    }

    /**
     * 移除赋值及其后面的逗号
     */
    private static String removeAssignmentAndFollowingComma(String setContent, int start, int end) {
        StringBuilder sb = new StringBuilder(setContent);

        // 先移除赋值本身
        sb.delete(start, end);

        // 查找赋值后面的逗号
        String remaining = sb.substring(start);
        Pattern followingCommaPattern = Pattern.compile("^\\s*,\\s*");
        Matcher matcher = followingCommaPattern.matcher(remaining);

        if (matcher.find()) {
            // 移除逗号
            sb.delete(start, start + matcher.end());
        }

        return sb.toString();
    }

    /**
     * 移除赋值及其前面的逗号
     */
    private static String removeAssignmentAndPrecedingComma(String setContent, int start, int end) {
        StringBuilder sb = new StringBuilder(setContent);

        // 查找赋值前面的逗号
        String beforeAssignment = sb.substring(0, start);
        Pattern precedingCommaPattern = Pattern.compile("\\s*,\\s*$");
        Matcher matcher = precedingCommaPattern.matcher(beforeAssignment);

        int deleteStart = start;
        if (matcher.find()) {
            // 包含前面的逗号一起删除
            deleteStart = matcher.start();
        }

        sb.delete(deleteStart, end);
        return sb.toString();
    }

    /**
     * 清理WHERE子句
     */
    private static String cleanWhereClause(String whereClause, Set<String> paramsToRemove) {
        String clause = whereClause.trim();

        // 移除WHERE关键字进行处理
        boolean hasWhereKeyword = clause.toLowerCase().startsWith("where");
        if (hasWhereKeyword) {
            clause = clause.substring(5).trim();
        }

        // 处理条件表达式
        clause = processConditions(clause, paramsToRemove);

        String result = clause.trim();
        if (hasWhereKeyword && !result.isEmpty()) {
            result = "WHERE " + result;
        }

        return result;
    }

    /**
     * 处理条件表达式 - 智能清理AND/OR操作符，支持括号嵌套
     */
    private static String processConditions(String conditions, Set<String> paramsToRemove) {
        if (conditions == null || conditions.trim().isEmpty()) {
            return conditions;
        }

        String result = conditions;

        // 使用迭代方式处理括号，避免递归栈溢出
        result = processParenthesesIteratively(result, paramsToRemove);

        return result;
    }

    /**
     * 迭代处理括号内容，避免递归栈溢出
     */
    private static String processParenthesesIteratively(String conditions, Set<String> paramsToRemove) {
        String result = conditions;
        boolean hasChanges = true;
        int maxIterations = 10; // 防止无限循环
        int iteration = 0;

        while (hasChanges && iteration < maxIterations) {
            hasChanges = false;
            iteration++;

            // 处理简单括号（不包含嵌套的括号）
            String beforeProcess = result;
            result = processSimpleParentheses(result, paramsToRemove);

            if (!result.equals(beforeProcess)) {
                hasChanges = true;
            }
        }

        // 最后处理剩余的条件
        result = removeConditionsDirectly(result, paramsToRemove);

        return result;
    }

    /**
     * 处理简单括号（内部不包含其他括号）
     */
    private static String processSimpleParentheses(String conditions, Set<String> paramsToRemove) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < conditions.length()) {
            char c = conditions.charAt(i);

            if (c == '(') {
                // 找到匹配的右括号
                int start = i;
                int level = 1;
                int j = i + 1;

                while (j < conditions.length() && level > 0) {
                    if (conditions.charAt(j) == '(') {
                        level++;
                    } else if (conditions.charAt(j) == ')') {
                        level--;
                    }
                    j++;
                }

                if (level == 0) {
                    // 找到完整的括号对
                    String insideParentheses = conditions.substring(i + 1, j - 1);

                    // 检查是否为简单括号（内部不含括号）
                    if (!insideParentheses.contains("(") && !insideParentheses.contains(")")) {
                        // 处理括号内容
                        String cleanedInside = removeConditionsDirectly(insideParentheses, paramsToRemove);
                        cleanedInside = cleanedInside.trim();

                        if (cleanedInside.isEmpty()) {
                            // 如果括号内容为空，跳过整个括号
                            // 但需要检查是否需要清理前后的操作符
                            result.append(" "); // 用空格占位，后续统一清理
                        } else {
                            result.append("(").append(cleanedInside).append(")");
                        }
                        i = j;
                    } else {
                        // 复杂括号，暂时保留
                        result.append(c);
                        i++;
                    }
                } else {
                    // 括号不匹配，直接添加
                    result.append(c);
                    i++;
                }
            } else {
                result.append(c);
                i++;
            }
        }

        return result.toString();
    }

    /**
     * 直接移除包含指定参数的条件
     */
    private static String removeConditionsDirectly(String segment, Set<String> paramsToRemove) {
        String result = segment;

        for (String paramToRemove : paramsToRemove) {
            result = removeSpecificCondition(result, paramToRemove);
        }

        return result;
    }

    /**
     * 移除特定参数的条件，智能处理AND/OR
     */
    private static String removeSpecificCondition(String segment, String paramToRemove) {
        // 构建匹配条件的正则表达式
        String[] patterns = {
                "\\s*\\w+\\s*(=|!=|<>|>|<|>=|<=|LIKE|IN|NOT\\s+IN)\\s*:" + paramToRemove + "(?!\\w)\\s*",
                "\\s*\\w+\\s*(=|!=|<>|>|<|>=|<=|LIKE|IN|NOT\\s+IN)\\s*#\\{" + paramToRemove + "\\}\\s*"
        };

        String result = segment;

        for (String patternStr : patterns) {
            // 使用简单的字符串替换方法，避免复杂的正则表达式
            result = removeConditionWithStringMatching(result, paramToRemove);
        }

        return result;
    }

    /**
     * 使用字符串匹配方式移除条件，避免复杂正则表达式
     */
    private static String removeConditionWithStringMatching(String segment, String paramToRemove) {
        String result = segment;

        // 查找参数的所有可能出现位置
        String[] paramFormats = {":" + paramToRemove, "#{" + paramToRemove + "}"};

        for (String paramFormat : paramFormats) {
            int index = result.indexOf(paramFormat);
            while (index >= 0) {
                // 找到条件的起始位置（向前找到字段名）
                int conditionStart = findConditionStart(result, index);
                // 找到条件的结束位置（参数后面可能有空格）
                int conditionEnd = findConditionEnd(result, index + paramFormat.length());

                if (conditionStart >= 0 && conditionEnd > conditionStart) {
                    // 提取完整条件
                    String condition = result.substring(conditionStart, conditionEnd);

                    // 移除条件及其相关操作符
                    result = removeConditionAndAdjustOperators(result, conditionStart, conditionEnd);
                }

                // 查找下一个出现位置
                index = result.indexOf(paramFormat, Math.max(0, conditionStart));
                if (index == result.indexOf(paramFormat)) {
                    // 如果找到的还是同一个位置，说明没有被移除，跳过避免无限循环
                    break;
                }
            }
        }

        return result;
    }

    /**
     * 查找条件的起始位置
     */
    private static int findConditionStart(String text, int paramIndex) {
        // 从参数位置往前找，寻找字段名的开始
        int i = paramIndex - 1;

        // 跳过操作符和空格
        while (i >= 0 && (Character.isWhitespace(text.charAt(i)) || "=!<>".indexOf(text.charAt(i)) >= 0)) {
            i--;
        }

        // 继续往前找到字段名的开始
        while (i >= 0 && (Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '_')) {
            i--;
        }

        return i + 1;
    }

    /**
     * 查找条件的结束位置
     */
    private static int findConditionEnd(String text, int startIndex) {
        int i = startIndex;

        // 跳过参数后面的空格
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }

        return i;
    }

    /**
     * 移除条件并调整AND/OR操作符
     */
    private static String removeConditionAndAdjustOperators(String text, int start, int end) {
        // 检查条件前后的AND/OR操作符
        String before = text.substring(0, start).trim();
        String after = text.substring(end).trim();

        boolean hasPrecedingOp = before.toUpperCase().endsWith(" AND") || before.toUpperCase().endsWith(" OR");
        boolean hasFollowingOp = after.toUpperCase().startsWith("AND ") || after.toUpperCase().startsWith("OR ");

        StringBuilder result = new StringBuilder();

        if (hasPrecedingOp && hasFollowingOp) {
            // 前后都有操作符，移除条件，保留一个操作符
            result.append(text.substring(0, start)).append(" ").append(text.substring(end));
        } else if (hasPrecedingOp) {
            // 只有前面的操作符，移除条件和前面的操作符
            String beforeTrimmed = before;
            if (before.toUpperCase().endsWith(" AND")) {
                beforeTrimmed = before.substring(0, before.length() - 4);
            } else if (before.toUpperCase().endsWith(" OR")) {
                beforeTrimmed = before.substring(0, before.length() - 3);
            }
            result.append(beforeTrimmed).append(" ").append(text.substring(end));
        } else if (hasFollowingOp) {
            // 只有后面的操作符，移除条件和后面的操作符
            String afterTrimmed = after;
            if (after.toUpperCase().startsWith("AND ")) {
                afterTrimmed = after.substring(4);
            } else if (after.toUpperCase().startsWith("OR ")) {
                afterTrimmed = after.substring(3);
            }
            result.append(text.substring(0, start)).append(" ").append(afterTrimmed);
        } else {
            // 没有操作符，直接移除条件
            result.append(text.substring(0, start)).append(" ").append(text.substring(end));
        }

        // 特殊处理：检查是否在WHERE后面留下了AND/OR
        String resultStr = result.toString();
        resultStr = resultStr.replaceAll("(?i)WHERE\\s+(AND|OR)\\s+", "WHERE ");

        return resultStr;
    }

    /**
     * 通用子句清理 - 智能处理操作符
     */
    private static String cleanGenericClause(String clause, Set<String> paramsToRemove) {
        String result = clause;

        // 如果包含AND/OR，按条件表达式处理
        if (result.toUpperCase().contains("AND") || result.toUpperCase().contains("OR")) {
            result = processConditions(result, paramsToRemove);
        } else {
            // 简单的参数替换
            for (String paramToRemove : paramsToRemove) {
                String[] patterns = {
                        "\\s*\\w+\\s*=\\s*:" + paramToRemove + "(?!\\w)",
                        "\\s*\\w+\\s*=\\s*#\\{" + paramToRemove + "\\}",
                        ":" + paramToRemove + "(?!\\w)",
                        "#\\{" + paramToRemove + "\\}"
                };

                for (String pattern : patterns) {
                    result = result.replaceAll(pattern, " ");
                }
            }
        }

        return result;
    }

    /**
     * 清理SQL中的WHERE子句
     */
    private static String cleanWhereInSQL(String sql, Set<String> paramsToRemove) {
        // 找到WHERE子句的位置
        Pattern wherePattern = Pattern.compile("(.*?)(\\s+WHERE\\s+.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = wherePattern.matcher(sql);

        if (matcher.find()) {
            String beforeWhere = matcher.group(1);
            String whereClause = matcher.group(2);

            String cleanedWhere = cleanWhereClause(whereClause, paramsToRemove);

            if (cleanedWhere.trim().equalsIgnoreCase("WHERE") || cleanedWhere.trim().isEmpty()) {
                return beforeWhere.trim();
            } else {
                return beforeWhere + " " + cleanedWhere;
            }
        }

        return sql;
    }

    /**
     * 最终清理：去除多余的操作符和空括号，修复WHERE后的语法问题
     */
    private static String finalClean(String sql) {
        String result = sql;

        // 清理多余的逗号（保留基本清理）
        result = result.replaceAll(",\\s*([\\)\\s]*(?:WHERE|FROM|ORDER|GROUP|HAVING|LIMIT|$))", "$1");
        result = result.replaceAll("SET\\s*,", "SET");
        result = result.replaceAll(",\\s*$", "");

        // 清理WHERE后直接跟AND/OR的情况
        result = result.replaceAll("(?i)WHERE\\s+(AND|OR)\\s+", "WHERE ");

        // 清理连续的AND/OR（更精确的模式）
        result = result.replaceAll("\\s+(AND|OR)\\s+(AND|OR)\\s+", " $2 ");

        // 清理括号边界的操作符
        result = result.replaceAll("\\(\\s*(AND|OR)\\s+", "(");
        result = result.replaceAll("\\s+(AND|OR)\\s*\\)", ")");

        // 清理行尾的AND/OR
        result = result.replaceAll("\\s+(AND|OR)\\s*([\\)\\s]*(?:ORDER|GROUP|HAVING|LIMIT|$))", "$2");

        // 清理空括号及其相关的AND/OR
        result = cleanEmptyParentheses(result);

        // 再次清理WHERE后可能出现的AND/OR（在清理空括号后）
        result = result.replaceAll("(?i)WHERE\\s+(AND|OR)\\s+", "WHERE ");

        // 清理多余的空格
        result = result.replaceAll("\\s+", " ");

        // 最终检查：如果WHERE后面没有条件，移除WHERE
        result = result.replaceAll("(?i)WHERE\\s*$", "");
        result = result.replaceAll("(?i)WHERE\\s+(?=ORDER|GROUP|HAVING|LIMIT)", "");

        return result.trim();
    }

    /**
     * 清理空括号及其相关的AND/OR操作符
     */
    private static String cleanEmptyParentheses(String sql) {
        String result = sql;
        boolean hasChanges = true;
        int maxIterations = 5;
        int iteration = 0;

        while (hasChanges && iteration < maxIterations) {
            hasChanges = false;
            iteration++;
            String before = result;

            // 清理空括号
            result = result.replaceAll("\\(\\s*\\)", " ");

            // 清理空括号前后的AND/OR
            result = result.replaceAll("\\s+(AND|OR)\\s+\\s+", " ");
            result = result.replaceAll("\\s+\\s+(AND|OR)\\s+", " ");

            if (!result.equals(before)) {
                hasChanges = true;
            }
        }

        return result;
    }

    /**
     * 获取空参数集合（包括null值、空字符串、空集合以及不存在的参数key）
     */
    private static Set<String> getEmptyParams(Map<String, Object> params) {
        Set<String> emptyParams = new HashSet<>();
        if (params != null) {
            // 检查存在的参数中的空值
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                Object value = entry.getValue();
                if (value == null ||
                        (value instanceof String && ((String) value).trim().isEmpty()) ||
                        (value instanceof Collection && ((Collection<?>) value).isEmpty()) ||
                        (value instanceof Object[] && ((Object[]) value).length == 0)) {
                    emptyParams.add(entry.getKey());
                }
            }
        }
        return emptyParams;
    }

    /**
     * 获取所有需要清理的参数（包括空参数和不存在的参数）
     */
    private static Set<String> getParamsToRemove(String sql, Map<String, Object> params) {
        Set<String> paramsToRemove = new HashSet<>();

        // 获取SQL中所有的参数名
        Set<String> allParamsInSQL = extractParams(sql);

        if (params == null) {
            // 如果参数Map为null，所有SQL中的参数都需要清理
            paramsToRemove.addAll(allParamsInSQL);
        } else {
            // 检查空参数
            paramsToRemove.addAll(getEmptyParams(params));

            // 检查不存在的参数key
            for (String paramInSQL : allParamsInSQL) {
                if (!params.containsKey(paramInSQL)) {
                    paramsToRemove.add(paramInSQL);
                }
            }
        }

        return paramsToRemove;
    }

    /**
     * 从SQL文本中提取参数名
     */
    private static Set<String> extractParams(String sql) {
        Set<String> params = new HashSet<>();
        Matcher matcher = PARAM_PATTERN.matcher(sql);
        while (matcher.find()) {
            String param = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            params.add(param);
        }
        return params;
    }

    /**
     * 分割UPDATE SQL
     */
    private static String[] splitUpdateSQL(String sql) {
        Pattern updatePattern = Pattern.compile("(UPDATE\\s+\\w+\\s+SET)\\s+(.*?)(?:\\s+(WHERE\\s+.*))?$",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher matcher = updatePattern.matcher(sql.trim());

        if (matcher.find()) {
            return new String[] {
                    matcher.group(1),           // UPDATE table SET
                    matcher.group(2),           // SET clause content
                    matcher.group(3)            // WHERE clause (可能为null)
            };
        }

        return new String[] { sql, "", "" };
    }

    // SQL类型判断方法
    private static boolean isUpdateSQL(String sql) {
        return sql.trim().toLowerCase().startsWith("update");
    }

    private static boolean isSelectSQL(String sql) {
        return sql.trim().toLowerCase().startsWith("select");
    }

    private static boolean isDeleteSQL(String sql) {
        return sql.trim().toLowerCase().startsWith("delete");
    }

    private static boolean isInsertSQL(String sql) {
        return sql.trim().toLowerCase().startsWith("insert");
    }

//    /**
//     * 测试方法
//     */
//    public static void main(String[] args) {
//        System.out.println("=== SQL清理测试 ===\n");
//
//        // 测试数据1：包含空参数和不存在的参数
//        Map<String, Object> paramsMap = new HashMap<>();
//        paramsMap.put("code", "TEST001");        // 有值
//        paramsMap.put("description", "desc");      // null参数
//        paramsMap.put("id", "1");                // 空字符串参数
//        paramsMap.put("name", "Test Name");      // 有值
//        // 注意：SQL中的status参数不在params1中
//
//        System.out.println("测试参数Map1: " + paramsMap);
//        System.out.println("（注意：SQL中可能包含不存在的参数如 :status）\n");
//
//        // 测试用例0：完整UPDATE SQL - 包含不存在的参数
//        String UPDATE = "UPDATE t set description = :description, code = :code " +
//                " where (description = :description or name = :name) and code = :code And id = :id";
//        System.out.println("原始UPDATE SQL:");
//        System.out.println(UPDATE);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(UPDATE, paramsMap));
//        System.out.println();
//
//
//        // 测试用例1：完整UPDATE SQL - 包含不存在的参数
//        String updateSQL = "UPDATE t SET description = :description, code = :code, status = :status WHERE code = :code AND id = :id AND status = :status";
//        System.out.println("原始UPDATE SQL:");
//        System.out.println(updateSQL);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(updateSQL, paramsMap));
//        System.out.println();
//
//        // 测试用例2：SET片段 - 包含不存在的参数
//        String setFragment = "description = :description, code = :code, status = :status, name = :name";
//        System.out.println("原始SET片段:");
//        System.out.println(setFragment);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(setFragment, paramsMap));
//        System.out.println();
//
//        // 测试用例3：WHERE片段 - 包含不存在的参数
//        String whereFragment = "description = :description AND code = :code AND status = :status AND id = :id";
//        System.out.println("原始WHERE片段:");
//        System.out.println(whereFragment);
//        // 测试用例6：测试精确的AND/OR清理逻辑
//        System.out.println("--- 测试精确的AND/OR清理逻辑 ---");
//
//        Map<String, Object> testParams = new HashMap<>();
//        testParams.put("code", "TEST001");
//        testParams.put("name", "Test");
//        // description 和 status 参数不存在
//
//        String[] testCases = {
//                "description = :description AND code = :code",  // 第一个参数空，应移除后面的AND
//                "code = :code AND description = :description",  // 第二个参数空，应移除前面的AND
//                "description = :description AND code = :code AND name = :name",  // 中间有空参数
//                "description = :description OR code = :code",   // 测试OR操作符
//                "code = :code OR description = :description OR name = :name",  // 多个OR
//                "description = :description AND code = :code OR status = :status AND name = :name"  // 混合AND/OR
//        };
//
//        for (String testCase : testCases) {
//            System.out.println("原始: " + testCase);
//            System.out.println("清理: " + cleanSQL(testCase, testParams));
//            System.out.println();
//        }
//
//        // 测试用例7：测试SET子句的逗号清理
//        System.out.println("--- 测试SET子句逗号清理 ---");
//        String[] setCases = {
//                "description = :description, code = :code, name = :name",  // 第一个参数空
//                "code = :code, description = :description, name = :name",  // 中间参数空
//                "code = :code, name = :name, description = :description",  // 最后参数空
//                "SET description = :description, code = :code, status = :status, name = :name"  // 带SET关键字
//        };
//
//        // 测试您提供的具体案例
//        System.out.println("--- 测试您提供的具体案例 ---");
//        String specificSQL = "UPDATE t set description = :description, code = :code " +
//                " where (description = :description or name = :name) and code = :code And id = :id";
//
//        Map<String, Object> specificParams = new HashMap<>();
//        specificParams.put("id", 5);
//        specificParams.put("description", null);  // 空参数
//        specificParams.put("name", "Rick");
//        // 注意：code 参数不存在
//
//        System.out.println("原始SQL:");
//        System.out.println(specificSQL);
//        System.out.println("参数: " + specificParams);
//        System.out.println("清理后: =============>");
//        String specificResult = cleanSQL(specificSQL, specificParams);
//        System.out.println(specificResult);
//        System.out.println();
//
//        // 测试更多括号内条件的案例
//        System.out.println("--- 测试更多括号内条件案例 ---");
//        String[] bracketTestCases = {
//                "(description = :description OR name = :name)",  // 括号内第一个条件为空
//                "(name = :name OR description = :description)",  // 括号内第二个条件为空
//                "(description = :description AND code = :code OR name = :name)",  // 复杂括号条件
//                "code = :code AND (description = :description OR status = :status) AND name = :name",  // 嵌套括号
//                "((description = :description OR name = :name) AND code = :code)",  // 双层嵌套
//                "(description = :description) AND (code = :code OR status = :status)"  // 多个括号组
//        };
//
//        // 测试您新提供的问题案例
//        System.out.println("--- 测试WHERE后AND的问题案例 ---");
//        String problemSQL = "UPDATE t set description = :description, code = :code " +
//                " where (description = :description or name = :name) and code = :code And id = :id";
//
//        Map<String, Object> problemParams = new HashMap<>();
//        problemParams.put("id", 5);
//        // description 和 name 参数不存在，会导致括号变空
//        problemParams.put("code", "C001");
//
//        System.out.println("原始SQL:");
//        System.out.println(problemSQL);
//        System.out.println("参数: " + problemParams);
//        System.out.println("清理后: =============>");
//        String problemResult = cleanSQL(problemSQL, problemParams);
//        System.out.println(problemResult);
//        System.out.println();
//
//        // 测试更多WHERE后AND/OR的边界情况
//        System.out.println("--- 测试WHERE边界情况 ---");
//        String[] whereBoundaryCases = {
//                "WHERE (description = :description) AND code = :code",  // 括号内容被清空
//                "WHERE description = :description AND (code = :code)",  // 第一个条件被清理
//                "WHERE (description = :description AND status = :status) AND code = :code",  // 复杂括号
//                "WHERE description = :description",  // 唯一条件被清理，WHERE应该被移除
//                "SELECT * FROM t WHERE description = :description ORDER BY id"  // WHERE后直接ORDER BY
//        };
//
//        for (String testCase : whereBoundaryCases) {
//            System.out.println("原始: " + testCase);
//            System.out.println("清理: " + cleanSQL(testCase, problemParams));
//            System.out.println();
//        }
//
//        // 测试数据2：参数Map为null的情况
//        System.out.println("--- 测试参数Map为null的情况 ---");
//        String sqlWithParams = "SELECT * FROM t WHERE code = :code AND name = :name";
//        System.out.println("原始SQL:");
//        System.out.println(sqlWithParams);
//        System.out.println("参数Map为null，清理后: =============>");
//        System.out.println(cleanSQL(sqlWithParams, null));
//        System.out.println();
//
//        // 测试数据3：包含空集合的参数
//        Map<String, Object> params3 = new HashMap<>();
//        params3.put("codes", Arrays.asList("A", "B"));     // 有值的集合
//        params3.put("ids", new ArrayList<>());             // 空集合
//        params3.put("names", null);                        // null
//        params3.put("types", new String[0]);               // 空数组
//
//        System.out.println("--- 测试空集合和数组参数 ---");
//        System.out.println("测试参数Map3: " + params3);
//        String sqlWithCollections = "SELECT * FROM t WHERE code IN (:codes) AND id IN (:ids) AND name = :names AND type IN (:types) AND status = :status";
//        System.out.println("原始SQL:");
//        System.out.println(sqlWithCollections);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(sqlWithCollections, params3));
//        System.out.println();
//
//        // 测试用例4：复杂嵌套条件
//        String complexSQL = "SELECT * FROM t WHERE (description = :description OR name = :name) AND (code = :code AND id = :id) AND status = :status";
//        System.out.println("--- 测试复杂嵌套条件 ---");
//        System.out.println("原始复杂SQL:");
//        System.out.println(complexSQL);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(complexSQL, paramsMap));
//        System.out.println();
//
//        // 测试用例5：INSERT SQL
//        String insertSQL = "INSERT INTO t (description, code, name, status) VALUES (:description, :code, :name, :status)";
//        System.out.println("--- 测试INSERT SQL ---");
//        System.out.println("原始INSERT SQL:");
//        System.out.println(insertSQL);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(insertSQL, paramsMap));
//
//        System.out.println(whereFragment);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(whereFragment, paramsMap));
//        System.out.println();
//
//        // 测试用例4：复杂WHERE条件
//        String complexWhere = "WHERE (description = :description OR name = :name) AND code = :code AND id = :id";
//        System.out.println("原始复杂WHERE:");
//        System.out.println(complexWhere);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(complexWhere, paramsMap));
//        System.out.println();
//
//        // 测试用例5：SELECT SQL
//        String selectSQL = "SELECT * FROM t WHERE description = :description AND code = :code AND id = :id";
//        System.out.println("原始SELECT SQL:");
//        System.out.println(selectSQL);
//        System.out.println("清理后: =============>");
//        System.out.println(cleanSQL(selectSQL, paramsMap));
//    }
}