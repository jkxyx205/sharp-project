package com.rick.admin.module.common.service;

import com.rick.admin.module.common.entity.CodeDescription;
import com.rick.common.util.Maps;
import com.rick.db.repository.EntityCodeDAO;
import com.rick.db.util.OperatorUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
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

    public void saveAll(@NotNull CodeDescription.CategoryEnum category, Collection<CodeDescription> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            for (CodeDescription codeDescription : list) {
                codeDescription.setCategory(category);
            }
        }
        // TODO
        codeDescriptionDAO.insertOrUpdate(list, "category", category.getCode());
    }

    public Optional<CodeDescription> findOne(CodeDescription.CategoryEnum category, String code) {
        return OperatorUtils.expectedAsOptional(codeDescriptionDAO.select("category = :category AND code = :code", Maps.of("category", category.getCode(), "code", code)));
    }

    public List<CodeDescription> findAll(CodeDescription.CategoryEnum category) {
        return codeDescriptionDAO.select("category = :category", Maps.of("category", category.getCode()));
    }

    public List<CodeDescription> findAll() {
        return codeDescriptionDAO.selectAll();
    }
}