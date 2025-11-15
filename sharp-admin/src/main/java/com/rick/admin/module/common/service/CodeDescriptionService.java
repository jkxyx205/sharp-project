package com.rick.admin.module.common.service;

import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.db.plugin.dao.core.EntityCodeDAO;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
        codeDescriptionDAO.insertOrUpdate(list, "category", category.getCode());
    }

    public Optional<CodeDescription> findOne(CodeDescription.CategoryEnum category, String code) {
        return OptionalUtils.expectedAsOptional(codeDescriptionDAO.selectByParams(Params.builder(1).pv("category", category.getCode()).pv("code", code).build()));
    }

    public List<CodeDescription> findAll(CodeDescription.CategoryEnum category) {
        return codeDescriptionDAO.selectByParams(Params.builder(1).pv("category", category.getCode()).build());
    }

    public List<CodeDescription> findAll() {
        return codeDescriptionDAO.selectAll();
    }
}