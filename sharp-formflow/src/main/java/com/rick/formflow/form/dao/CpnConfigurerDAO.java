package com.rick.formflow.form.dao;

import com.rick.db.plugin.dao.core.EntityDAOImpl;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Repository;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * @author Rick
 * @createdAt 2021-11-03 11:22:00
 */

@Repository
public class CpnConfigurerDAO extends EntityDAOImpl<CpnConfigurer, Long> {

    @Override
    public List<CpnConfigurer> selectByParams(Map<String, ?> params, String conditionSQL) {
        List<CpnConfigurer> cpnConfigurers = super.selectByParams(params, conditionSQL);
        for (CpnConfigurer cpnConfigurer : cpnConfigurers) {
            decodeOptions(cpnConfigurer.getOptions());
        }
        return cpnConfigurers;
    }

    @Override
    public int update(CpnConfigurer entity) {
        encodeOptions(entity.getOptions());
        return super.update(entity);
    }

    @Override
    public int[] update(Collection<CpnConfigurer> collection) {
        encodeCollectionOptions(collection);
        return super.update(collection);
    }

    @Override
    public int insert(CpnConfigurer entity) {
        encodeOptions(entity.getOptions());
        return super.insert(entity);
    }

    @Override
    public int[] insert(Collection<?> paramsList) {
        encodeCollectionOptions((Collection<CpnConfigurer>) paramsList);
        return super.insert(paramsList);
    }

    @Override
    public int[] insertOrUpdate(Collection<CpnConfigurer> entities) {
        return super.insertOrUpdate(entities);
    }

    private void encodeCollectionOptions(Collection<CpnConfigurer> entities) {
        for (Object cpnConfigurer : entities) {
            encodeOptions(((CpnConfigurer)cpnConfigurer).getOptions());
        }
    }

    private void encodeOptions(String[] options) {
        if (ArrayUtils.isNotEmpty(options)) {
            try {
                for (int i = 0; i < options.length; i++) {
                    options[i] = URLEncoder.encode(options[i], "utf-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void decodeOptions(String[] options) {
        if (ArrayUtils.isNotEmpty(options)) {
            try {
                for (int i = 0; i < options.length; i++) {
                    options[i] = URLDecoder.decode(options[i], "utf-8");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
}