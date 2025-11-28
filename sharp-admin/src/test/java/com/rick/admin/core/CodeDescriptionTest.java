package com.rick.admin.core;

import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.admin.module.common.service.CodeDescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * @author Rick.Xu
 * @date 2023/5/28 15:07
 */
@SpringBootTest
public class CodeDescriptionTest {

    @Autowired
    private CodeDescriptionService codeDescriptionService;

    @Test
    public void testSave() {
        codeDescriptionService.saveAll(CodeDescription.CategoryEnum.MATERIAL, Arrays.asList(
                CodeDescription.builder().code("MM1").description("物料组1").sort(0).build(),
                CodeDescription.builder().code("MM3").description("物料组3").sort(1).build(),
                CodeDescription.builder().code("MM4").description("物料组4").sort(2).build()
        ));
    }

    @Test
    public void testSave2() {
        codeDescriptionService.saveAll(CodeDescription.CategoryEnum.PURCHASING_ORG, Arrays.asList(
                CodeDescription.builder().code("PG1").description("采购组织1").sort(0).build(),
                CodeDescription.builder().code("M1").description("采购组织2").sort(1).build()
        ));
    }

}
