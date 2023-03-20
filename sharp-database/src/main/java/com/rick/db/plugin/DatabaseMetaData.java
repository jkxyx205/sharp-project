package com.rick.db.plugin;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2023-03-13 14:05:00
 */
public class DatabaseMetaData {

    public static Map<String, List<String>> tableColumnMap;

    public static Map<String, String> tablePrimaryKeyMap;

    public static void initTableMapping(JdbcTemplate jdbcTemplate) {
        // 获取所有字段
        java.sql.DatabaseMetaData databaseMetaData;
        tableColumnMap = new HashMap<>();
        tablePrimaryKeyMap = new HashMap<>();

        try {
            databaseMetaData = jdbcTemplate.getDataSource().getConnection().getMetaData();

            try(ResultSet resultSet = databaseMetaData.getTables(null, null, "%", new String[]{})){
                while(resultSet.next()) {
                    tableColumnMap.put(resultSet.getString("TABLE_NAME"), null);
                    tablePrimaryKeyMap.put(resultSet.getString("TABLE_NAME"), null);
                }
            }

            for (String tableName : tableColumnMap.keySet()) {
                try(ResultSet columns = databaseMetaData.getColumns("",null, tableName, null)){
                    List<String> columnNameList = new ArrayList<>();

                    while(columns.next()) {
                        String columnName = columns.getString("COLUMN_NAME");
                        columnNameList.add(columnName);
                    }
                    tableColumnMap.put(tableName, columnNameList);
                }

                try(ResultSet primaryKeys = databaseMetaData.getPrimaryKeys("", null, tableName)){
                    while(primaryKeys.next()){
                        String primaryKeyColumnName = primaryKeys.getString("COLUMN_NAME");
                        tablePrimaryKeyMap.put(tableName, primaryKeyColumnName);
                        break;
                    }
                }
            }


        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }

}
