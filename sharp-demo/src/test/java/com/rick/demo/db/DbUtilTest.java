package com.rick.demo.db;

import com.rick.db.plugin.DbUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/4/30 13:27
 */
@SpringBootTest
public class DbUtilTest {

    @Autowired
    private DbUtils mysqlDbUtil;

    @Test
    public void testQuery() {
        List<Object[]> rows = mysqlDbUtil.executeQuery("select * from cat where id = ?", new Object[]{706172121272848384L});
        for (Object[] row : rows) {
            for (Object value : row) {
                System.out.print(value + ", ");
            }
            System.out.println("------");
        }
    }

    @Test
    public void testUpdate() {
        int rows = mysqlDbUtil.executeUpdate("update cat set name = ? where id = ?", new Object[]{"Rick", 706172604947402752L});
        System.out.println("affect rows " + rows);
    }
}
