package com.rick.admin.core;

import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.admin.module.demo.entity.ComplexModel;
import com.rick.admin.module.demo.model.EmbedValue;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2024/8/18 04:29
 */
@SpringBootTest
public class ComplexModelTest {

    @Autowired
    private EntityDAO<ComplexModel, Long> complexModelDAO;

    @Test
    public void testComplexModel() {
        complexModelDAO.insert(ComplexModel.builder()
                        .name("Rick2")
                        .materialType(new DictValue("HIBE"))
                        .unit(new DictValue("EA"))
                        .categoryDict(new DictValue("SALES_ORG"))
                .category(CodeDescription.CategoryEnum.MATERIAL).build());
    }

    @Test
    public void testSelectComplexModel() {
        Optional<ComplexModel> optional = complexModelDAO.selectById(856759492044419072L);
        ComplexModel complexModel = optional.get();
        System.out.println(complexModel);
        // 获取label
        complexModel.setEmbedValue(new EmbedValue(new DictValue("HIBE")));
        DictUtils.fillDictLabel(complexModel);
        System.out.println(complexModel);
    }
}
