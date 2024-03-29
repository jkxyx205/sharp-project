package com.rick.formflow.form.dao;

import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.formflow.form.cpn.core.FormCpn;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.List;


/**
 * @author Rick
 * @createdAt 2021-11-03 17:13:00
 */

@Repository
public class FormCpnDAO extends EntityDAOImpl<FormCpn, Long> {

    public List<FormCpn> listByFormId(Long formId) {
        Assert.notNull(formId, "formId cannot be null");
        return selectByParams(FormCpn.builder().formId(formId).build(), "form_id = :formId ORDER BY order_num asc");
    }

    public int deleteByFormId(Long formId) {
        Assert.notNull(formId, "formId cannot be null");
        return delete("form_id", Arrays.asList(formId));
    }

}