package com.rick.db.formatter;

import com.rick.db.dto.PageModel;
import com.rick.db.util.NamedParameterUtils;
import com.rick.db.util.ParsedSql;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @author Rick.Xu
 *
 */
public abstract class AbstractSqlFormatter {

	private static final String PARAM_IN_SEPARATOR = ",";
	
	private static final int QUERY_IN_MAX_COUNT = 1000;

	private static final String BEFORE_END = "(?<![\\w.]+)";

	private static final String AFTER_END = "(?!\\w+)";

	/**
	 * 合法名字：数字字母下划线
	 */
//	private static final String LEGAL_NAME = "\\w+";
		
	private static final String COLUMN_REGEX = "((?i)(to_char|NVL)?\\s*([(][^([(]|[)])]*[)])|[a-zA-Z0-9'[.]_[-]]+)";

	/**
	 * 逻辑操作符
	 */
	private static final String OPERATOR_REGEX = "(?i)(like|!=|>=|<=|<|>|=|\\s+in|\\s+not\\s+in|regexp)";

    /**
     * 合法的变量名称:name
     */
    private static final String LEGAL_PARAM_NAME_REGEX = "[a-zA-Z]+\\w*";

    /**
     * 参数占位符 :name or (:name)
     */
    private static final String HOLDER_REGEX = "(([(]\\s*:\\w+\\s*[)])|(:\\w+))";

    /**
     * 参数占位符 ${name} or (${name})
     */
    private static final String HOLDER_REGEX2 = "(([(]\\s*\\$\\{\\s*\\w+\\s*\\}\\s*[)])|(\\$\\{\\s*\\w+\\s*\\}))";

    /**
     * :name
     */
    private static final String PARAM_REGEX = ":" + LEGAL_PARAM_NAME_REGEX;

    /**
     * ${name}
     */
    private static final String PARAM_REGEX2 = "\\{\\s*("+LEGAL_PARAM_NAME_REGEX+")\\s*\\}";

    /**
	 * 逻辑IN和NOT IN操作符
	 */
	private static final String OPERATOR_IN_REGEX = "(?i)(\\s+in|\\s+not\\s+in)";
	
	private static final String FULL_REGEX = new StringBuilder().append(COLUMN_REGEX).append("\\s*").append(OPERATOR_REGEX).append("\\s*").append(HOLDER_REGEX).toString();

	private static final String FULL_REGEX2 = new StringBuilder().append(COLUMN_REGEX).append("\\s*").append(OPERATOR_REGEX).append("\\s*").append(HOLDER_REGEX2).toString();

	private static final String IN_FULL_REGEX = new StringBuilder().append(COLUMN_REGEX).append("\\s*").append(OPERATOR_IN_REGEX).append("\\s*")
		    .append("[(][^)]+[)]").toString();
	
	private static final Map<String,String> DATE_FORMAT_MAP;

	static {
		DATE_FORMAT_MAP = new HashMap<>(2);
		DATE_FORMAT_MAP.put("\\d{4}/\\d{2}/\\d{2}", "yyyy/MM/dd");
		DATE_FORMAT_MAP.put("\\d{4}-\\d{2}-\\d{2}", "yyyy-MM-dd");
	}
	
	public String formatSql(String srcSql, Map<String, ?> params, Map<String, Object> formatMap) {
		ParsedSql parsedSql = NamedParameterUtils.parseSqlStatement(srcSql);
		List<String> names = parsedSql.getParameterNames();
		
		if(params == null) {
			params = Collections.emptyMap();
		}

		if (formatMap == null) {
			formatMap = Collections.emptyMap();
		}

		srcSql = handleHolderSQL(srcSql, params);

		List<ParamHolder> paramList = splitParam(srcSql, FULL_REGEX, PARAM_REGEX);

		for(ParamHolder h : paramList) {
			String name = h.param;
			names.remove(name);
			Object param = params.get(name);

            if(Objects.isNull(param)) {
                srcSql = ignoreAndReturnSQL(srcSql, h);
                continue;
            }

			Object value = "";

			if(param.getClass() == String[].class) {
				String[] values = (String[])param;
				if(values.length > 0) {
					StringBuilder sb = new StringBuilder();
					for(String s : values) {
						sb.append(s).append(PARAM_IN_SEPARATOR);
					}
					value = sb.toString();
				}
			} else if(param instanceof Instant) {
                value = Timestamp.from((Instant) param);
            } else {
				value = param;
			}

            if(value.getClass() == String.class && StringUtils.isBlank((CharSequence)value)) {
                srcSql = ignoreAndReturnSQL(srcSql, h);
                continue;
            }

			//if has the value
			//String format;
			if(h.operator.toUpperCase().endsWith("IN")) {
                Set<Object> set = new HashSet();

                if (value instanceof Iterable) {
                    Iterable iterable = (Iterable) value;
                    for(Object v : iterable) {
                        set.add(v);
                    }
                } else if (value.getClass().isArray()) {
                    int length = Array.getLength(value);
                    for (int i = 0; i < length; i ++) {
                        set.add(Array.get(value, i));
                    }
                } else { // 其他当字符串处理
                    String strValue = String.valueOf(value);
                    set.addAll(Arrays.asList(strValue.split(PARAM_IN_SEPARATOR)));
                }

				if (set.size() > 0) {
					StringBuilder sb = new StringBuilder("IN (");
					int i = 0;
					for (Object inParam : set) {
                        String newProName = name + i;
                        sb.append(":").append(newProName).append(",");
                        formatMap.put(newProName, inParam);
                        i++;
                    }
					sb.deleteCharAt(sb.length()-1);
					sb.append(")");
					sb.toString();
					srcSql = srcSql.replaceAll("((?i)in)\\s*[(]\\s*:" + h.param + "\\s*[)]", sb.toString());
				}

			} else if("LIKE".equalsIgnoreCase(h.operator)) {
				 srcSql = srcSql.replace(h.full, new StringBuilder("UPPER(").append(h.column).append(") ").append(h.operator).append(contactString(name)).append(" ").append(escapeString()));
				 formatMap.put(name, likeEscape(value + ""));
			} else {
				formatMap.put(name, value);
			}
		}

        // 其他解决不了的 用''
		for (String p : names) {
			p = ":"+p;
			String pp = p.substring(1);
			Object v = formatMap.get(pp) ;
			if (v == null || StringUtils.isBlank((String)v)) {
				v = params.get(pp);
				if (v == null || (v instanceof String && StringUtils.isBlank((String)v))) {
                     StringBuilder sb  = new StringBuilder(srcSql);
                     int len = p.length();
                     int index = 0;
                     int strLen = sb.length();
                     while ((index = sb.indexOf(p,index)) > -1) {
                         int nextLen = index + len;
                         char nextChar = ' ';
                         if (strLen > nextLen) {
                             nextChar = sb.charAt(nextLen);
                         }

                         if (nextChar < 'A' || nextChar > 'z') {
                             sb.delete(index, index + len);
                             sb.insert(index, "''");
                             index = index + 2;
                         } else {
                             index = index + len;
                         }
                     }
                    srcSql = sb.toString();
				} else {
					formatMap.put(pp, v);
				}
			}
		}

        //mysql变量变成?
		return right(changeInSQL(srcSql));
	}

    public String formatSqlCount(String srcSql) {
		StringBuilder sb = new  StringBuilder();
		sb.append("SELECT COUNT(0) FROM (").append(removeOrders(srcSql)).append(") temp");
		return sb.toString();
	}
	
	private static List<ParamHolder> splitParam(String sql, String fullRegex, String paramRegex) {
		Pattern pat = Pattern.compile(fullRegex);
		Matcher mat = pat.matcher(sql);  
		List<ParamHolder> paramList = new ArrayList<ParamHolder>();
		
		while (mat.find()) {
			 ParamHolder holder = new ParamHolder();
			 String matchRet = mat.group().trim();
			 holder.full = matchRet;
			 //再进行拆分列名
			 Pattern pat1 = Pattern.compile("^" + COLUMN_REGEX);
			 Matcher mat1 = pat1.matcher(matchRet);
			 while(mat1.find()) {
				 String matchRet1 = mat1.group().trim();
				 holder.column = matchRet1;
			 }
			 
			 //再进行拆分出变量
			 Pattern pat2 = Pattern.compile(OPERATOR_REGEX);
			 Matcher mat2 = pat2.matcher(matchRet);
			 while(mat2.find()) {
				 String matchRet2 = mat2.group().trim();
				 holder.operator = matchRet2;
			 }

			//再进行拆分出「变量」
			 Pattern pat3 = Pattern.compile(paramRegex);
			 Matcher mat3 = pat3.matcher(matchRet);
			 while(mat3.find()) {
				 String matchRet3 = mat3.group().trim();
                 Matcher mat4 = Pattern.compile("(:|\\{\\s*)(\\w+)(\\s*})?").matcher(matchRet3);
                 mat4.find();
                 holder.param  = mat4.group(2);
			 }
            paramList.add(holder);
        }

		return paramList;
	}

	private static String likeEscape(String str) {
		return str.replaceAll("%", "\\\\%")
				.replaceAll("_", "\\\\_");
	}

    private static class ParamHolder {

        private String full;

        private String column;

        private String operator;

        private String param;

        @Override
        public String toString() {
            return new StringBuilder().append(full).append("|").append(column).append("|").append(operator).append("|").append(param).toString();
        }
    }

	public abstract String pageSql(String sql, PageModel model);

	public abstract String contactString(String name);

	public abstract String escapeString();

    public static String changeInSQL(String sql) {
        Pattern pat = Pattern.compile(IN_FULL_REGEX);
        Matcher mat = pat.matcher(sql);

        while (mat.find()) {
            ParamHolder holder = new ParamHolder();
            String matchRet = mat.group().trim();
            holder.full = matchRet;
            //再进行拆分
            Pattern pat1 = Pattern.compile("^" + COLUMN_REGEX);
            Matcher mat1 = pat1.matcher(matchRet);
            while (mat1.find()) {
                String matchRet1 = mat1.group().trim();
                holder.column = matchRet1;
            }

            //再进行拆分
            Pattern pat2 = Pattern.compile(OPERATOR_REGEX);
            Matcher mat2 = pat2.matcher(matchRet);
            while (mat2.find()) {
                String matchRet2 = mat2.group().trim();
                holder.operator = matchRet2;
            }

            //再进行拆分
            Pattern pat3 = Pattern.compile("[(].*[)]");
            Matcher mat3 = pat3.matcher(matchRet);
            while (mat3.find()) {
                String matchRet3 = mat3.group().trim();
                holder.param = matchRet3;
            }

            //
            StringBuilder newInSQL = new StringBuilder();
            newInSQL.append("(");

            String[] params = holder.param.replaceAll("[(]|[)]", "").split(",");

            if (params.length <= QUERY_IN_MAX_COUNT) {
                continue;
            } else {
                int cn = params.length % QUERY_IN_MAX_COUNT == 0 ? params.length / QUERY_IN_MAX_COUNT : params.length / QUERY_IN_MAX_COUNT + 1;
                for (int i = 0; i < cn; i++) {
                    newInSQL.append(holder.column).append(" ").append(holder.operator).append("(");
                    //
                    for (int j = 1; j <= QUERY_IN_MAX_COUNT; j++) {
                        if (i * QUERY_IN_MAX_COUNT + j <= params.length) {
                            newInSQL.append(params[i * QUERY_IN_MAX_COUNT + j - 1]).append(",");
                        }
                    }
                    newInSQL.deleteCharAt(newInSQL.length() - 1);

                    newInSQL.append(") OR ");
                }
                newInSQL.delete(newInSQL.length() - 4, newInSQL.length());

                newInSQL.append(")");

                sql = sql.replace(holder.full, newInSQL.toString());
            }
        }
        return sql;
    }

    public String wrapSordString(String sql, String sidx, String sord) {
        StringBuilder sb = new StringBuilder("SELECT * FROM (");
        sb.append(sql).append(") temp");
        if (StringUtils.isNotBlank(sidx) && StringUtils.isNotBlank(sord)) {
            sb.append(" ORDER BY ").append(sidx).append(" ").append(sord);
            return sb.toString();
        } else {
            return sql;
        }
    }

    /**
     * 去除hql的orderBy子句。
     *
     * @param
     * @return
     */
    protected String removeOrders(String sqlString) {
        Pattern p = Pattern.compile("order\\s*by\\s+(" + COLUMN_REGEX + ")+(\\s+(desc|asc)(?!\\w+))?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(sqlString);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * 整体检查并格式化SQL
     *
     * @param sql
     * @return
     */
    private String right(String sql) {
        sql = rightParentheses(sql);
        sql = rightUpdate(sql);
        return sql;
    }

    private String rightUpdate(String sql) {
        return sql.replaceAll("(?i)set[\\s*,]+", "SET ")
                .replaceAll("(?i)[,\\s*]+where"," WHERE");
    }

    /**
     * 递归处理小括号
     *
     * @return
     */
    private String rightParentheses(String sql) {
        if (sql.matches("(?s).*\\(\\s*\\).*")) {
            return rightParentheses(replace(sql, "\\(\\s*\\)"));
        }
        return sql;
    }

    private boolean isOnlyCause(String sql, String condition) {
        String regexRight = "(?is).*" + condition + "\\s*(and|or)(?!\\w+).*";
        String regexLeft = "(?is).*(and|or)(?!\\w+)\\s*" + condition + ".*";
        return !(sql.matches(regexLeft) || sql.matches(regexRight));
    }

    private boolean isLastCause(String sql, String condition) {
        String regexLeft = "(?is).*(and|or)(?!\\w+)\\s*" + condition + ".*";
        return sql.matches(regexLeft);
    }

    private String replace(String srcSql, String condition) {
        if (isOnlyCause(srcSql, condition)) {
            return srcSql.replaceAll("(?i)(where)?\\s*" + condition + "", "");
        } else if (isLastCause(srcSql, condition)) {
            return srcSql.replaceAll("(?i)(and|or)\\s*" + condition + "", "");
        } else {
            return srcSql.replaceAll("(?i)\\s*" + condition + "\\s*(and|or)", "");
        }
    }

    private static final char PREFIX_START =  '$';
    private static final char PREFIX_END =  '{';
    private static final char SUFFIX = '}';

    private String handleHolderSQL(String srcSQL, Map<String, ?> params) {
        List<ParamHolder> paramList = splitParam(srcSQL, FULL_REGEX2, PARAM_REGEX2);

        Set<String> paramSet = paramList.stream().map(ph -> ph.param).collect(Collectors.toSet());

        char[] sqlChar = srcSQL.toCharArray();
        int len = sqlChar.length;

        StringBuilder sqlBuilder = new StringBuilder(len);

        StringBuilder holderBuilder = new StringBuilder();

        boolean findHolder = false;

        for (int i = 0; i < len; i++) {
            char c = sqlChar[i];
            if (PREFIX_START == c) {
                if (i + 1 < len && PREFIX_END == sqlChar[i + 1]) {
                    findHolder = true;
                }
            } else if (findHolder == true && SUFFIX == c) {
                findHolder = false;
                String holderName = holderBuilder.toString();
                Object holderValue = params.get(holderBuilder.toString());

                // 判断是否是参数
                boolean isExpression = paramSet.contains(holderName);
                String resultText = isExpression ? ":param" + i + Long.MAX_VALUE : (Objects.isNull(holderValue) ? "" : String.valueOf(holderValue));

                sqlBuilder.append(resultText);
                holderBuilder.delete(0, holderBuilder.length());
                continue;
            }

            if (findHolder && PREFIX_START != c && PREFIX_END != c && SUFFIX != c && c != ' ') {
                holderBuilder.append(c);
            }

            if (!findHolder) {
                sqlBuilder.append(c);
            }
        }

        return sqlBuilder.toString();
    }

    private String ignoreAndReturnSQL(String srcSql, ParamHolder h) {
        String fullRegex = BEFORE_END + h.full.replace(".", "\\.").replace("(", "\\(").replace(")", "\\)") + AFTER_END;
        srcSql = replace(srcSql, fullRegex);
        return srcSql;
    }

}
