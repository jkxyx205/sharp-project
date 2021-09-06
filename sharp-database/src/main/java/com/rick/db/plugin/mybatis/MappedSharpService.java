package com.rick.db.plugin.mybatis;

import com.rick.db.service.GridService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.Map;

/**
 * @author Rick
 * @version 1.0.0
 * @Description 从mapper.xml中获取SQL，并利用Mybatis的动态SQL特性
 * @createTime 2020-12-23- 13:47:00
 *   NOTE: gridService不能用这种变量形式 id = #{id} 或 id = ?，只能用id = :id
 *     <select id="findById">
 *         select
 *         <include refid="column" />
 *         from t_project where
 *                      id=:id
 *         <if test="title != null and title !=''">
 *             and title like ${title}
 *         </if>
 *     </select>
 *     <sql id="column">
 *         id, description, 'haha' as title
 *     </sql>
 */
@RequiredArgsConstructor
public class MappedSharpService {

    private final SqlSessionFactory sqlSessionFactory;

    private final GridService gridService;

    public <T> T handle(String selectId, Map<String, Object> params, SharpServiceHandler<T> sharpServiceHandler) {
        MappedStatement mappedStatement = sqlSessionFactory.getConfiguration()
                .getMappedStatement(selectId);

        BoundSql boundSql = mappedStatement.getBoundSql(params);
        String sql = boundSql.getSql();
        return sharpServiceHandler.handle(gridService, sql, params);
    }
}
