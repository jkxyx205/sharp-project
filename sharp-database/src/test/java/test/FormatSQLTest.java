package test;

import com.rick.db.formatter.OracleSqlFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/13/20 1:14 AM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public class FormatSQLTest {

    public static void main(String[] args) {
        String s1 = "select * from a where DELETED = '0' and title like :title and hell=23 order by created_time desc";
        String s2 = "select * from a where hello=:keyword order by created_time desc";
        String s3 = "select * from a where (DELETED = '0' and title like :title) and hell=23 order by created_time desc";
        String s4 = "select * from a where (DELETED = :s and title like :title) and hell=23 order by created_time desc";
        String s5 = "select * from a where hello=:keyword and wwe=:we order by created_time desc";
        String s6 = "select * from a where hello=:keyword";
        String s7 = "select * from a where (hello=:keyword)order by created_time desc";
        String s8 = "select * from a where ((hello=:keyword))";
        String s9 = "select * from a where ((hello=:keyword and c = :h)) and ((abc = :a) or (a =1))";
        String s10 = "update sm_user set a = :a, sm.age=:ageq where sm_user.name = :name and age=:age";

        OracleSqlFormatter oracleSqlFormatter = new OracleSqlFormatter();

        Map<String, Object> returnMap =  new HashMap<>();

        Map<String, Object> params = new HashMap<>();
        params.put("a", "hello");
        params.put("ageq", "hello222");
        params.put("name", "hello222");
        System.out.println(oracleSqlFormatter.formatSql(s1, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s2, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s3, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s4, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s5, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s6, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s7, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s8, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s9, params, returnMap));
        System.out.println(oracleSqlFormatter.formatSql(s10, params, returnMap));
    }
}
