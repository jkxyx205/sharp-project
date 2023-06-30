package com.rick.demo;

import com.rick.common.util.StringUtils;
import com.rick.demo.module.book.entity.Cat;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilsTest {

    @Test
    public void testToCamel() {
        assertThat(StringUtils.stringToCamel("OPEN")).isEqualTo("open");
        assertThat(StringUtils.stringToCamel("RoleType")).isEqualTo("roleType");
        assertThat(StringUtils.stringToCamel("roleType")).isEqualTo("roleType");
        assertThat(StringUtils.stringToCamel("role")).isEqualTo("role");
        assertThat(StringUtils.stringToCamel("role_type")).isEqualTo("roleType");
        assertThat(StringUtils.stringToCamel("ROLE_TYPE")).isEqualTo("roleType");
        assertThat(StringUtils.stringToCamel("ROLE_TYpE")).isEqualTo("roleType");
        assertThat(StringUtils.stringToCamel("document_extend_reference_id")).isEqualTo("documentExtendReferenceId");

//        assertThat(StringUtils.snakeToCamel("tom.OPEN")).isEqualTo("tom.open");
//        assertThat(StringUtils.snakeToCamel("tomNew.OPEN")).isEqualTo("tomNew.open");
//        assertThat(StringUtils.snakeToCamel("TOMNEW.OPEN")).isEqualTo("tomnew.open");
//
//        assertThat(StringUtils.snakeToCamel("roleType")).isEqualTo("roleType");
//        assertThat(StringUtils.snakeToCamel("role")).isEqualTo("role");

//        new Date("1679303674985");
//        new Date("1679502443913");

//        System.out.println(new Date().getTime());
    }

    @Test
    public void testList() {
        List<Cat.Reward> list = Arrays.asList(Cat.Reward.builder().title("吃饭冠军").build());



//        Class<?>[] classGenericsTypes = ClassUtils.getClassGenericsTypes(list.getClass());
//        for (Class<?> classGenericsType : classGenericsTypes) {
//            System.out.println(classGenericsType);
//        }
    }
}
