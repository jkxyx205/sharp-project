package com.rick.db.plugin.dao.support;

import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-11-01 11:06:00
 */
@UtilityClass
public class SelectCascadeContextHolder {

    private ThreadLocal<Map<String, Object>> selectCascadeContext = new ThreadLocal() {
        @Override
        protected Object initialValue() {
            return Maps.newHashMapWithExpectedSize(3);
        }
    };

    private static String SELECT_DIRECT_SRC_KEY = "SelectDirectSrc";
    private static String SELECT_DIRECT_TARGET_KEY = "SelectDirectTarget";
    private static String SELECT_DIRECT_SRC_DATA = "SelectDirectSrcData";

    public <T> void setSelectDirectAndData(String srcTable, String targetTable, List<T> data) {
        selectCascadeContext.get().put(SELECT_DIRECT_SRC_KEY, srcTable);
        selectCascadeContext.get().put(SELECT_DIRECT_TARGET_KEY, targetTable);
        selectCascadeContext.get().put(SELECT_DIRECT_SRC_DATA, data);
    }

    public <T> boolean canCascadeSelect(String srcTable, String targetTable, List<T> data) {
        if (selectCascadeContext.get().size() == 0) {
            return true;
        }

        return selectCascadeContext.get().get(SELECT_DIRECT_SRC_DATA) != data;

//        if (selectCascadeContext.get().get(SELECT_DIRECT_SRC_KEY).equals(targetTable) &&
//                selectCascadeContext.get().get(SELECT_DIRECT_TARGET_KEY).equals(srcTable)) {
//            return false;
//        }

//        return true;
    }



}
