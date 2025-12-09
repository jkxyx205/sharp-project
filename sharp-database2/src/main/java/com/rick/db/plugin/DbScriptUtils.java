package com.rick.db.plugin;

import lombok.experimental.UtilityClass;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 导入 sql
 * @author Rick.Xu
 * @date 2025/1/15 11:23
 */
@UtilityClass
public class DbScriptUtils {

    public static void importSQL(Connection connection, String sqlContent) throws IOException, SQLException {
        importSQL(connection, new StringReader(sqlContent));
    }

    public static void importSQL(Connection connection, File sqlFile) throws IOException, SQLException {
        importSQL(connection, new FileReader(sqlFile));
    }

    public static void importSQL(Connection connection, Reader reader) throws IOException, SQLException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                // Skip comments and empty lines
                if (line.startsWith("--") || line.isEmpty()) {
                    continue;
                }
                sqlBuilder.append(line).append("\r\n");
                // If a statement ends with a semicolon, execute it
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString();
                    try (Statement statement = connection.createStatement()) {
                        statement.execute(sql);
//                        System.out.println("Executed: " + sql);
                    }
                    sqlBuilder.setLength(0); // Clear the builder for the next statement
                }
            }
        }
    }
}
