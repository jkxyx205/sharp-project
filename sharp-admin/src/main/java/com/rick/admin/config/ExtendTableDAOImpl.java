package com.rick.admin.config;

import com.rick.admin.auth.common.UserContextHolder;
import com.rick.admin.sys.user.entity.User;
import com.rick.db.repository.EntityDAO;
import com.rick.db.repository.EntityDAOManager;
import com.rick.db.repository.TableDAO;
import com.rick.db.repository.TableDAOImpl;
import com.rick.db.repository.support.SqlHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2025/8/27 21:17
 */
public class ExtendTableDAOImpl extends TableDAOImpl implements TableDAO {

    private Map<String, EntityDAO<?, ?>> tableNameDAOMap;

    public ExtendTableDAOImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        super(namedParameterJdbcTemplate);
    }

    @Override
    public int update(String tableName, String columns, String condition, Object... args) {
        return getNamedParameterJdbcTemplate().getJdbcTemplate().update("UPDATE " + tableName + " SET " + columns + SqlHelper.buildWhere(condition), args);
    }

    @Override
    public int update(String tableName, String columns, String condition, Map<String, ?> paramMap) {
        HashMap<String, Object> mergedParamMap = new HashMap<>(paramMap);
        mergedParamMap.put("baseEntityInfo.updateBy", getUserId());
        mergedParamMap.put("baseEntityInfo.updateTime", LocalDateTime.now());
        mergedParamMap.put("baseEntityInfo.deleted", false);
        return getNamedParameterJdbcTemplate().update("UPDATE " + tableName + " SET " + columns + SqlHelper.buildWhere(condition), mergedParamMap);
    }

    @Override
    public int insert(String tableName, String columnNames, Map<String, ?> paramMap) {
        return super.insert(tableName, columnNames, addInsertInfo(tableName, paramMap));
    }

    @Override
    public Number insertAndReturnKey(String tableName, String columnNames, Map<String, ?> paramMap, String... idColumnName) {
        return super.insertAndReturnKey(tableName, columnNames, addInsertInfo(tableName, paramMap), idColumnName);
    }

    @Override
    public int delete(String tableName, String condition, Object... args) {
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            return update(tableName, "is_deleted = ?", condition, ArrayUtils.addAll(new Object[]{true}, args));
        } else {
            return super.delete(tableName, condition, args);
        }
    }

    @Override
    public int delete(String tableName, String condition, Map<String, ?> paramMap) {
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            Map<String, Object> mergedParamMap = new HashMap<>(paramMap);
            mergedParamMap.put("deleted", true);
            return update(tableName, "is_deleted = :deleted", condition, mergedParamMap);
        } else {
            return super.delete(tableName, condition, paramMap);
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        tableNameDAOMap =  EntityDAOManager.getAllEntityDAO().stream().collect(Collectors.toMap(entityDAO -> entityDAO.getTableMeta().getTableName(), Function.identity()));
    }

    private Map<String, Object> addInsertInfo(String tableName, Map<String, ?> paramMap) {
        Map<String, Object> mergedParamMap = new HashMap<>(paramMap);
        if (Objects.nonNull(tableNameDAOMap.get(tableName))) {
            LocalDateTime now = LocalDateTime.now();
            long userId = getUserId();

            mergedParamMap.put("create_by", userId);
            mergedParamMap.put("create_time", now);
            mergedParamMap.put("update_by", userId);
            mergedParamMap.put("update_time", now);
        }

        mergedParamMap.put("is_deleted", false);
        return mergedParamMap;
    }

    public long getUserId() {
        User user = UserContextHolder.get();
        user = (user == null) ? User.builder().id(1L).build() : user;
        return user.getId();
    }

}
