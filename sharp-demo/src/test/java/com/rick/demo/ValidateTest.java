package com.rick.demo;

import com.rick.common.validate.ValidatorHelper;
import com.rick.demo.module.project.domain.entity.User;
import com.rick.demo.module.project.service.ProjectService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.validation.ConstraintViolationException;


@SpringBootTest
public class ValidateTest {

    @Autowired
    private ValidatorHelper validatorHelper;

    @Autowired
    private ProjectService projectService;

    @Test
    public void testValidate() {
        User user = User.builder().name("rick").phone("18898987765").build();
        validatorHelper.validate(user);
    }

    @Test
    public void testValidate2() {
        Assertions.assertThatThrownBy(() -> {
            User user = User.builder().name("Rick").phone("188").build();
            validatorHelper.validate(user);
        }).isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void testValidate3() {
        User user = User.builder().name("rick").phone("18898987765").sex(1).userStatus("LOCKED").build();
        validatorHelper.validate(user);
    }

    @Test
    public void testValidate4() {
        Assertions.assertThatThrownBy(() -> {
            User user = User.builder().name("rick").phone("18898987765").sex(3).userStatus("ENABLED").build();
            validatorHelper.validate(user);
        }).isInstanceOf(ConstraintViolationException.class);
    }
}