package com.rick.demo.module.project.controller;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
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
 * @author Rick
 * @createdAt 2021-11-09 19:22:00
 */
@RestController
@RequestMapping("sql")
public class SqlController {

    private static final Map<String, String> sqlMapping = new HashMap<>();

    static {
        sqlMapping.put("project1", "select title,description,cover_url,owner_id,sex,address,status,list,phone_number,map,id FROM t_project1 WHERE id = :id and title like :title");
    }

    @GetMapping("{key}")
    public Result result(@PathVariable String key, HttpServletRequest request) {
        String sql = sqlMapping.get(key);
        if (Objects.isNull(sql)) {
            return ResultUtils.exception(-1, "没有找到"+key+"对应的sql");
        }

        return ResultUtils.success(GridUtils.list(sql, HttpServletRequestUtils.getParameterMap(request)));
    }

}
