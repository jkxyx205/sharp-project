package com.rick.demo.module.embedded.dao;

import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.demo.module.embedded.entity.Vendor;
import org.springframework.stereotype.Repository;

/**
 * @author Rick
 * @createdAt 2023-03-06 17:59:00
 */

@Repository
public class VendorDAO extends EntityDAOImpl<Vendor, Long> {

}