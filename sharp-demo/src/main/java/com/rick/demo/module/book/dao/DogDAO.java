package com.rick.demo.module.book.dao;

import com.rick.db.repository.EntityDAOImpl;
import com.rick.demo.module.book.entity.Dog;
import org.springframework.stereotype.Repository;

/**
 * @author Rick.Xu
 * @date 2023/8/22 01:37
 */
@Repository
public class DogDAO extends EntityDAOImpl<Dog, Long> {

}