package com.rick.db.plugin;

import com.rick.db.plugin.model.IdCodeValue;
import com.rick.db.plugin.model.IdValue;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import com.rick.db.util.OptionalUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.util.Assert;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 根据 id 批量获取描述信息
 * @author Rick.Xu
 * @date 2024/11/15 14:19
 */
public final class IdDescriptionUtils {

    private static SharpService sharpService;

    private IdDescriptionUtils() {}

    public static void setSharpService(SharpService sharpService) {
        if (IdDescriptionUtils.sharpService != null) {
            throw new BeanCreationException("bean has init already!");
        }

        IdDescriptionUtils.sharpService = sharpService;
    }

    public static Map<Long, IdCodeValue> queryIdCodeValueAsMap(List<Long> ids, String table, String descriptionColumnName) {
        return queryIdCodeValueAsMap(ids, table, "id", "code", descriptionColumnName);
    }

    public static Map<Long, IdCodeValue> queryIdCodeValueAsMap(List<Long> ids, String table, String idColumnName, String codeColumnName, String descriptionColumnName) {
        List<IdCodeValue> idCodeValueList = queryIdCodeValue(ids, table, idColumnName, codeColumnName, descriptionColumnName);
        return idCodeValueList.stream().collect(Collectors.toMap(idCodeValue -> idCodeValue.getId(), Function.identity()));
    }

    public static Optional<IdCodeValue> queryIdCodeValue(Long id, String table, String descriptionColumnName) {
        return queryIdCodeValue(id, table, "id", "code", descriptionColumnName);
    }

    public static Optional<IdCodeValue> queryIdCodeValue(Long id, String table, String idColumnName, String codeColumnName, String descriptionColumnName) {
        if (Objects.isNull(id)) {
            return Optional.empty();
        }
        return OptionalUtils.expectedAsOptional(queryIdCodeValue(Arrays.asList(id), table, idColumnName, codeColumnName, descriptionColumnName));
    }

    /**
     *
     * @param ids id 的值
     * @param table 表
     * @param idColumnName
     * @param codeColumnName
     * @param descriptionColumnName
     * @return
     */
    public static List<IdCodeValue> queryIdCodeValue(List<Long> ids, String table, String idColumnName, String codeColumnName, String descriptionColumnName) {
        Assert.notEmpty(ids, "ids cannot be empty");
        String sql = "SELECT "+idColumnName+" as id, "+codeColumnName+" as code, "+descriptionColumnName+" as description FROM "+table+" WHERE "+idColumnName+" IN (:ids)";
        return sharpService.query(sql, Params.builder(1).pv("ids", ids).build(), IdCodeValue.class);
    }

    public static Map<Long, IdValue> queryIdValueAsMap(List<Long> ids, String table, String descriptionColumnName) {
        return queryIdValueAsMap(ids, table, "id", descriptionColumnName);
    }

    public static Map<Long, IdValue> queryIdValueAsMap(List<Long> ids, String table, String idColumnName, String descriptionColumnName) {
        List<IdValue> idCodeValueList = queryIdValue(ids, table, idColumnName, descriptionColumnName);
        return idCodeValueList.stream().collect(Collectors.toMap(idCodeValue -> idCodeValue.getId(), Function.identity()));
    }

    public static Optional<IdValue> queryIdValue(Long id, String table, String descriptionColumnName) {
        return queryIdValue(id, table, "id", descriptionColumnName);
    }

    public static Optional<IdValue> queryIdValue(Long id, String table, String idColumnName, String descriptionColumnName) {
        if (Objects.isNull(id)) {
            return Optional.empty();

        }
        return OptionalUtils.expectedAsOptional(queryIdValue(Arrays.asList(id), table, idColumnName, descriptionColumnName));
    }

    public static List<IdValue> queryIdValue(List<Long> ids, String table, String idColumnName, String descriptionColumnName) {
        Assert.notEmpty(ids, "ids cannot be empty");
        String sql = "SELECT "+idColumnName+" as id, "+descriptionColumnName+" as description FROM "+table+" WHERE "+idColumnName+" IN (:ids)";
        return sharpService.query(sql, Params.builder(1).pv("ids", ids).build(), IdValue.class);
    }
}
