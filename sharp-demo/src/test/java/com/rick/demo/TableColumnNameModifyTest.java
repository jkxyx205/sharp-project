package com.rick.demo;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * @author Rick.Xu
 * @date 2023/4/24 13:51
 */

public class TableColumnNameModifyTest {

    @Test
    public void testReadTables() throws Exception {
        String path = "/Users/rick/Space/tmp/tables";

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));

        String table;
        while ((table = reader.readLine()) != null) {
            System.out.println(generatorSql(table));
            System.out.println();
            System.out.println();
        }
    }

    private String generatorSql(String tableName) {
        String sql = "alter table %s\n" +
                "        change create_id create_by bigint null;\n" +
                "        alter table %s\n" +
                "        change update_id update_by bigint null;";


        return String.format(sql, tableName, tableName, tableName, tableName);
    }

//    private String generatorSql(String tableName) {
//        String sql = "alter table %s\n" +
//                "        change create_by create_by bigint null;\n" +
//                "\n" +
//                "        alter table %s\n" +
//                "        change created_at create_time timestamp null;\n" +
//                "\n" +
//                "        alter table %s\n" +
//                "        change update_by update_by bigint null;\n" +
//                "\n" +
//                "        alter table %s\n" +
//                "        change updated_at update_time timestamp null;";
//
//
//        return String.format(sql, tableName, tableName, tableName, tableName);
//    }
}
