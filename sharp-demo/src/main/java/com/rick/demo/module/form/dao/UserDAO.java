package com.rick.demo.module.form.dao;

import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.demo.module.form.entity.User;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2023-03-16 23:57:00
 */

@Repository
public class UserDAO extends EntityDAOImpl<User, Long> {

}