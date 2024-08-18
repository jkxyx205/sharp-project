package com.rick.admin.common.api;

import com.rick.meta.dict.entity.Dict;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 获取字典数据源
 * @author Rick.Xu
 * @date 2024/8/14 11:13
 */
@RestController
@RequestMapping("dicts")
@RequiredArgsConstructor
public class DictApi {

    /**
     * 多个code 用 , 分割
     * @param codes
     * @return
     */
    @GetMapping
    public Map<String, List<Dict>> list(String codes) {
        if (StringUtils.isNotBlank(codes)) {
            return dictService.getDictsByCodes(Arrays.asList(codes.split(",")));
        }
        return Collections.emptyMap();
    }

    private final DictService dictService;

}
