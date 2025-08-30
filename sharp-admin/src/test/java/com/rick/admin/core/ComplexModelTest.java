package com.rick.admin.core;

import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.admin.module.demo.entity.ComplexModel;
import com.rick.admin.module.demo.model.EmbeddedValue;
import com.rick.db.repository.EntityDAO;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.*;

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
        List<Map<String, Object>> attachmentList = new ArrayList<>();
        HashMap<String, Object> attachment1 = new HashMap<>();
        attachment1.put("url", "baidu.com");
        attachment1.put("name", "picture");
        attachmentList.add(attachment1);

        List<List<String>> schoolExperienceList = new ArrayList<>();
        schoolExperienceList.add(Arrays.asList("2023-11-11", "苏州大学"));
        schoolExperienceList.add(Arrays.asList("2019-11-11", "苏州中学"));

        complexModelDAO.insert(ComplexModel.builder()
                .id(856759492044419072L)
                .name("Rick2")
                .age(34)
                .birthday(LocalDate.of(2021, 12, 26))
                .categoryList(Arrays.asList(CodeDescription.CategoryEnum.MATERIAL, CodeDescription.CategoryEnum.PURCHASING_ORG))
                .marriage(true)
                .attachmentList(attachmentList)
                .schoolExperienceList(schoolExperienceList)
                .map(attachment1)
                .materialType(new DictValue("HIBE"))
                .unit(new DictValue("EA"))
                .categoryDict(new DictValue("SALES_ORG"))
                .categoryDictList(Arrays.asList(new DictValue("MATERIAL"), new DictValue("SALES_ORG")))
                .embeddedValue(new EmbeddedValue(new DictValue("HIBE"), "texg"))
                .category(CodeDescription.CategoryEnum.MATERIAL)
                .workStatus(ComplexModel.WorkStatusEnum.FINISHED).build());
    }

    @Test
    public void testSelectComplexModel() {
        Optional<ComplexModel> optional = complexModelDAO.selectById(856759492044419072L);
        ComplexModel complexModel = optional.get();
        System.out.println(complexModel);
        // 获取label
//        complexModel.setEmbeddedValue(new EmbeddedValue(new DictValue("HIBE"), "texg"));
        DictUtils.fillDictLabel(complexModel);
        System.out.println(complexModel);
    }
}
