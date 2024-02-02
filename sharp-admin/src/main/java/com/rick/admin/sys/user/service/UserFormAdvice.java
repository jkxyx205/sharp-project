package com.rick.admin.sys.user.service;

import com.google.common.collect.Lists;
import com.rick.admin.auth.common.AuthConstants;
import com.rick.admin.sys.role.entity.Role;
import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.bo.FormBO;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2023/6/3 08:50
 */
@Component
@RequiredArgsConstructor
public class UserFormAdvice implements FormAdvice {

    private final DictService dictService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public void beforeInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        values.put("password", passwordEncoder.encode(AuthConstants.DEFAULT_PASSWORD));

        List<String> roleIdList = (List<String>) values.get("roleIds");
        List<Role> roleList = Lists.newArrayListWithExpectedSize(roleIdList.size());
        for (String id : roleIdList) {
            roleList.add(Role.builder().id(Long.parseLong(id)).build());
        }
        values.put("roleList", roleList);
    }

    @Override
    public void afterInstanceHandle(FormBO form, Long instanceId, Map<String, Object> values) {
        dictService.rebuild("sys_user");
    }
}
