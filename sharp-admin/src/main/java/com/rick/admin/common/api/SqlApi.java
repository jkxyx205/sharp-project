package com.rick.admin.common.api;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.db.dto.Grid;
import com.rick.db.plugin.GridUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Rick.Xu
 * @date 2023/6/14 11:13
 */
@RestController
@RequestMapping("api")
public class SqlApi {

    private static final Map<String, String> sqlMapping = new HashMap<>();

    static {
        sqlMapping.put("project", "select title,description,cover_url,owner_id,sex,address,status,list,phone_number,map,id FROM t_project1 WHERE id = :id and title like :title");
    }

    @GetMapping("{key}")
    public Grid<Map<String, Object>> list(@PathVariable String key, HttpServletRequest request) {
        String sql = sqlMapping.get(key);
        if (Objects.isNull(sql)) {
            throw new ResourceNotFoundException(key);
        }

        return GridUtils.list(sql, HttpServletRequestUtils.getParameterMap(request));
    }

}
