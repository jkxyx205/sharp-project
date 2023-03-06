package com.rick.demo.module.embedded.dao;

import com.rick.db.plugin.dao.core.BaseDAOImpl;
import com.rick.demo.module.embedded.entity.Company;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2023-03-06 14:17:00
 */

@Repository
public class CompanyDAO extends BaseDAOImpl<Company, Long> {

}