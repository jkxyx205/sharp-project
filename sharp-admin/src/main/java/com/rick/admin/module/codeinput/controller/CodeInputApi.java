package com.rick.admin.module.codeinput.controller;

import com.rick.admin.module.codeinput.service.CodeInputService;
import com.rick.common.http.HttpServletRequestUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 获取code input 通用的接口
 * @author Rick.Xu
 * @date 2023/6/14 11:13
 */
@RestController
@RequestMapping("codeInput")
@RequiredArgsConstructor
public class CodeInputApi {

    private final CodeInputService codeInputService;

    @GetMapping("{key}")
    public Map<String, Object> codeSearchResult(@PathVariable String key, @RequestParam String code, HttpServletRequest request) {
        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
        return codeInputService.codeSearchResult(key, code, params);
    }

    @GetMapping("{key}/dialog")
    public Map<String, Object> dialogSearchResult(@PathVariable String key, HttpServletRequest request) {
        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request);
        return codeInputService.dialogSearchResult(key, params);
    }
}
