package com.rick.admin.module.common.service;

import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.common.util.Maps;
import com.rick.db.repository.EntityCodeDAO;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/2/20 15:51
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class CodeDescriptionService {

    EntityCodeDAO<CodeDescription, Long> codeDescriptionDAO;

    public void saveAll(CodeDescription.CategoryEnum category, Collection<CodeDescription> list) {
        // TODO
//        codeDescriptionDAO.insertOrUpdate(list, "category", category.getCode());
    }

    public List<CodeDescription> findAll(CodeDescription.CategoryEnum category) {
        return codeDescriptionDAO.select("category = :category", Maps.of("category", category.getCode()));
    }

    public List<CodeDescription> findAll() {
        return codeDescriptionDAO.selectAll();
    }
}