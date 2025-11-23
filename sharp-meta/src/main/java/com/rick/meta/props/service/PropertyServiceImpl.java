package com.rick.meta.props.service;

import com.rick.db.repository.TableDAO;
import com.rick.meta.props.model.KeyValueProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;


/**
 * @author Rick
 * @createdAt 2021-09-06 17:16:00
 */
@RequiredArgsConstructor
@Slf4j
public class PropertyServiceImpl implements PropertyService, InitializingBean {

    private final TableDAO tableDAO;

    private static final String INSERT_SQL = "INSERT INTO sys_property(name, value) VALUES (?, ?)";

    private static final String UPDATE_SQL = "UPDATE sys_property SET value = ? WHERE name = ?";

    private static final String SELECT_SQL = "SELECT name, value FROM sys_property";

    private final KeyValueProperties keyValueProperties;

    @Override
    public String getProperty(String name) {
        return PropertyUtils.getProperty(name);
//        if (StringUtils.isBlank(name)) {
//            return null;
//        }
//
//        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
//        params.put("name", name);
//
//        Optional<PropertyDO> optional = sharpService
//                .queryForObject(SELECT_SQL,
//                        params,
//                        PropertyDO.class);
//
//        return optional.isPresent() ? optional.get().getValue() : null;
    }

    @Override
    public void setProperty(String name, String value) {
        Assert.hasText(name, "property name must has text");

        JdbcTemplate jdbcTemplate = tableDAO.getNamedParameterJdbcTemplate().getJdbcTemplate();

        int updateCount = jdbcTemplate.update(UPDATE_SQL, value, name);

        if (updateCount == 0) {
            jdbcTemplate.update(INSERT_SQL, name, value);
        }

        // build
        PropertyUtils.map.put(name, value);
    }

    @Override
    public void afterPropertiesSet() {
        // 初始化
        // 配置文件属性
        PropertyUtils.map.putAll(keyValueProperties.getItems());
        // 数据库属性
        try {
            PropertyUtils.map.putAll(tableDAO.selectForKeyValue(SELECT_SQL, null));
        } catch (Exception e) {
            log.warn("sys_property表没有创建成功！");
        }
    }
}
