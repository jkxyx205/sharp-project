package com.rick.demo;

import com.rick.demo.test.IServiceProvider;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rick.Xu
 * @date 2023/9/15 21:22
 */
public class SimpleTest {

    @Test
    public void testSpi() {
        ServiceLoader<IServiceProvider> loader = ServiceLoader.load(IServiceProvider.class);
        for (IServiceProvider iServiceProvider : loader) {
           iServiceProvider.sayHello("Rick");
        }
    }

    @Test
    public void getTableBySql() {
        String sql = "select id, code, name from sys_user where code like :code OR name like :code";
//        String sql = "SELECT id, code, name FROM sys_user WHERE code like :code OR name like :code";

//        Pattern pattern = Pattern.compile("from\\s+\\w+\\s+where", Pattern.CASE_INSENSITIVE);
        Pattern pattern = Pattern.compile("from\\s+([a-zA-Z][a-zA-Z0-9_]{0,63})\\s+where", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            String tableName = matcher.group(0).split("\\s+")[1];
            System.out.println(tableName);
        }
    }


}
