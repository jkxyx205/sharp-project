package com.rick.admin.config;


import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.validate.ValidatorHelper;
import com.rick.db.plugin.dao.support.ColumnAutoFill;
import com.rick.db.plugin.dao.support.DefaultColumnAutoFill;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validator;
import java.util.Map;

/**
 * @author Rick
 * @date  2021-03-23 23:58:00
 */
@Configuration
@RequiredArgsConstructor
public class SharpConfig {

    private final Validator validator;

    @Bean
    public ColumnAutoFill<Object> fill() {
        return new ColumnAutoFill<Object>() {
            @Override
            public Map<String, Object> insertFill(String idPropertyName, Object id) {
                Map<String, Object> fill = new DefaultColumnAutoFill().insertFill(idPropertyName, id);

                fill.put("create_by", getUserId());
                fill.put("update_by", getUserId());
                return fill;
            }

            @Override
            public Map<String, Object> updateFill() {
                Map<String, Object> fill = new DefaultColumnAutoFill().updateFill();
                fill.put("update_by", getUserId());
                return fill;
            }
        };
    }

    public long getUserId() {
        User user = UserContextHolder.get();
        user = (user == null) ? User.builder().id(1L).build() : user;
        return user.getId();
    }

    @Bean
    public ValidatorHelper validatorHelper() {
        return new ValidatorHelper(validator);
    }

}
