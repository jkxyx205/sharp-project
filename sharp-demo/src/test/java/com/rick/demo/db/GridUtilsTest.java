package com.rick.demo.db;

import com.rick.db.dto.Grid;
import com.rick.db.plugin.GridUtils;
import com.rick.db.plugin.SQLUtils;
import com.rick.excel.plugin.ExportUtils;
import com.rick.excel.table.model.MapTableColumn;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GridUtilsTest {

    /**
     * 列求和
     */
    @Test
    public void testSummary() {
        List<BigDecimal> list = GridUtils.numericObject("SELECT SUM(work_time) FROM t_demo", null);
        assertThat(list).isNotNull();
        assertThat(list.get(0)).isEqualTo(BigDecimal.valueOf(9));
    }

    /**
     * 查询列表
     */
    @Test
    public void testList() {
        Map params = new HashMap<>();
        // 通用参数
        params.put("page", 1);
        params.put("size", 2);
        params.put("sidx", "title");
        params.put("sord", "desc");

        // 业务参数
        params.put("title", "hello");

//        Grid list = GridUtils.list("SELECT id, title FROM t_demo WHERE title like :title", params, null, new String[]{"title"});
        Grid list = GridUtils.list("SELECT id, title FROM t_demo WHERE title like :title", params, null, "title");
        assertThat(list.getRecords()).isEqualTo(1);
    }

    /**
     * 查询根据多个id删除记录
     */
    @Test
    public void testDeleteByIn() {
        int deletedCount = SQLUtils.delete("t_demo", "id", Arrays.asList(1358619329104297985L, 1358619736027283457L));
        assertThat(deletedCount).isEqualTo(0);
    }

    /**
     * 查询sql导出
     */
    @Test
    public void testSqlExport() throws IOException {
        Map params = new HashMap<>();
        // 业务参数
        params.put("title", "version");

        ExportUtils.export("SELECT id, title FROM t_demo WHERE title like :title", params,
                new FileOutputStream("/Users/rick/Downloads/3.xlsx"),
                Lists.newArrayList(
                        new MapTableColumn("title", "标题")
//                        new MapTableColumn("id", "ID")
                ));
    }
}