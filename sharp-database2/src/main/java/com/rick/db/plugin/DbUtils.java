package com.rick.db.plugin;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Rick.Xu
 * @date 2024/4/30 10:13
 */
public final class DbUtils {

    private String url;
    private String username;
    private String password;

    private DataSource dataSource;

    public DbUtils(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public DbUtils(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public int executeUpdate(String sql) {
        return executeUpdate(sql, null);
    }

    public int executeUpdate(String sql, Object[] params) {
        return executeUpdate(sql, params, null);
    }

    public int executeUpdate(String sql, Object[] params, Consumer<PreparedStatement> consumer) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                if (params != null && params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        preparedStatement.setObject(i + 1, params[i]);
                    }
                }
                int affectRows = preparedStatement.executeUpdate();
                if (consumer != null) {
                    consumer.accept(preparedStatement);
                }
                return affectRows;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<Object[]> executeQuery(String sql) {
        return executeQuery(sql, null);
    }

    public List<Object[]> executeQuery(String sql, Object[] params) {
        return execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                if (params != null && params.length > 0) {
                    for (int i = 0; i < params.length; i++) {
                        preparedStatement.setObject(i + 1, params[i]);
                    }
                }
                try (ResultSet resultSet = preparedStatement.executeQuery();){
                    ResultSetMetaData rsmd = resultSet.getMetaData();
                    int columnCount = rsmd.getColumnCount();

                    List<Object[]> list = new ArrayList<>();
                    while (resultSet.next()) {
                        Object[] row = new Object[columnCount];
                        for (int i = 0; i < columnCount; i++) {
                            row[i] = resultSet.getObject(i + 1);
                        }
                        list.add(row);
                    }

                    return list;
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public <T> T execute(Function<Connection, T> consumer) {
        try (Connection connection = dataSource == null ? DriverManager.getConnection(url, username, password) : dataSource.getConnection()) {
            return consumer.apply(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new RuntimeException();
    }
}
