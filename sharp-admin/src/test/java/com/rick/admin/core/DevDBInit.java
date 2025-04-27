package com.rick.admin.core;

import com.rick.db.plugin.DbScriptUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 测试数据库创建：
 *
 * 1. 服务器 sql 备份，sql 文件名为 bak-20250407235959.sql
 * 2. 将服务器 sql 文件下载到 /Users/rick/Space/Share，然后执行 test
 *  - 按照日期找最新的备份 sql
 *  - 修改数据库名 sharp-admin-[备份的日期] 比如 sharp-admin-20250407
 *  - bak-20250407235959.sql 关于数据库名 sharp-admin 替换成 sharp-admin-20250407
 *  - 执行 替换后的 sql
 *
 * @author Rick.Xu
 * @date 2024/11/6 19:17
 */
@Slf4j
public class DevDBInit {

    private static String folder = "/Users/rick/Space/Share";

    @Test
    public void init() throws IOException, SQLException {
        long start = System.currentTimeMillis();
        File folder = new File(DevDBInit.folder);
        File[] files = folder.listFiles(pathname -> pathname.getName().matches("bak-.*\\.sql"));

        Arrays.sort(files, Comparator.comparing(File::getName));

        File sqlFile = files[files.length - 1];

        String database = "sharp-admin-" + sqlFile.getName().substring(4, 12);
        log.info("database = {}", database);

        String sqlContent = FileUtils.readFileToString(sqlFile, "utf-8");

//        FileUtils.writeStringToFile(new File(DBInit.folder, database + "-format.sql"),
//                sqlContent.replaceAll("sharp-admin", database),
//                "utf-8");

        DataSource dataSource = dataSource();
        DbScriptUtils.importSQL(dataSource.getConnection(), sqlContent.replaceAll("sharp-admin", database));
        long end = System.currentTimeMillis();
        System.out.println("processed at costs "+((end - start) / 1000)+" ms");

        // mysql -uroot -p123456 sharp-admin-20241106 < /Users/rick/Space/Share/sharp-admin-20241106.sqlFile
    }

    private DataSource dataSource() {
        // 创建数据源
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://127.0.0.1:3306");
        dataSource.setUsername("root");
        dataSource.setPassword("123456");
        return dataSource;
    }
}
