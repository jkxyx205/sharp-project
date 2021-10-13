package com.rick.meta.props.service;

import com.google.common.collect.Maps;
import com.rick.db.service.SharpService;
import com.rick.meta.props.dao.dataobject.PropertyDO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Optional;

/**
 * @author Rick
 * @createdAt 2021-09-06 17:16:00
 */
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final SharpService sharpService;

    private static final String INSERT_SQL = "INSERT INTO sys_property(name, value) VALUES (?, ?)";

    private static final String UPDATE_SQL = "UPDATE sys_property SET value = ? WHERE name = ?";

    private static final String SELECT_SQL = "SELECT name, value FROM sys_property WHERE name = :name";

    @Override
    public String getProperty(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("name", name);

        Optional<PropertyDO> optional = sharpService
                .queryForObject(SELECT_SQL,
                        params,
                        PropertyDO.class);

        return optional.isPresent() ? optional.get().getValue() : null;
    }

    @Override
    public void setProperty(String name, String value) {
        Assert.hasText(name, "property name must has text");

        JdbcTemplate jdbcTemplate = sharpService.getNamedJdbcTemplate().getJdbcTemplate();

        int updateCount = jdbcTemplate.update(UPDATE_SQL, value, name);

        if (updateCount == 0) {
            jdbcTemplate.update(INSERT_SQL, name, value);
        }
    }
}
