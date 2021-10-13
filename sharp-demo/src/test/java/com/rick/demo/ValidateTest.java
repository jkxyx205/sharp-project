package com.rick.demo;

import com.rick.common.validate.ValidatorHelper;
import com.rick.demo.module.project.domain.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.validation.BindException;


@SpringBootTest
public class ValidateTest {

    @Autowired
    private ValidatorHelper validatorHelper;

    @Test
    public void testValidate() throws BindException {
        User user = User.builder().name("rick").phone("18898987765").build();
        validatorHelper.validate(user);
    }

    @Test
    public void testValidate2() {
        Assertions.assertThatThrownBy(() -> {
            User user = User.builder().name("Rick").phone("188").build();
            validatorHelper.validate(user);
        }).isInstanceOf(BindException.class);
    }

    @Test
    public void testValidate3() throws BindException {
        User user = User.builder().name("rick").phone("18898987765").sex(1).userStatus("LOCKED").build();
        validatorHelper.validate(user);
    }

    @Test
    public void testValidate4() throws BindException, IllegalAccessException, InstantiationException {
        Assertions.assertThatThrownBy(() -> {
            User user = User.builder().name("rick").phone("18898987765").sex(3).userStatus("ENABLED").build();
            validatorHelper.validate(user);
        }).isInstanceOf(BindException.class);
    }

}