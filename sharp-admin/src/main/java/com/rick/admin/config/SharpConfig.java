package com.rick.admin.config;


import com.rick.common.validate.ValidatorHelper;
import com.rick.db.repository.ExtendTableDAOImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.validation.Validator;

/**
 * @author Rick
 * @date  2021-03-23 23:58:00
 */
@Configuration
@RequiredArgsConstructor
public class SharpConfig {

    private final Validator validator;

    @Bean
    public ExtendTableDAOImpl tableDAO(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new ExtendTableDAOImpl(namedParameterJdbcTemplate);
    }

    @Bean
    public ValidatorHelper validatorHelper() {
        return new ValidatorHelper(validator);
    }

}
