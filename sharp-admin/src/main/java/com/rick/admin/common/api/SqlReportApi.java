package com.rick.admin.common.api;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.db.plugin.page.Grid;
import com.rick.db.plugin.page.GridUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * sql报表
 * @author Rick.Xu
 * @date 2023/6/14 11:13
 */
@RestController
@RequestMapping("api")
public class SqlReportApi {

    private static final Map<String, String> sqlMapping = new HashMap<>();

    static {
        // 必须把 id 查询出来
        sqlMapping.put("sys_document", "select id, name FROM sys_document WHERE id = :id and name like :name");
    }

    @GetMapping("sql/{key}")
    public Grid<Map<String, Object>> list(@PathVariable String key, HttpServletRequest request) {
        String sql = sqlMapping.get(key);
        if (Objects.isNull(sql)) {
            throw new ResourceNotFoundException(key);
        }

        return GridUtils.list(sql, HttpServletRequestUtils.getParameterMap(request));
    }

}
