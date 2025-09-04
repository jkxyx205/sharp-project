package com.rick.demo.module.book.service;

import com.rick.db.repository.EntityDAO;
import com.rick.demo.module.book.entity.Cat;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * @author Rick.Xu
 * @date 2023/7/1 10:15
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class CatService {

    EntityDAO<Cat, Long> catDAO;

    public Cat save(Cat cat) {
       return catDAO.insert(cat);
    }

}