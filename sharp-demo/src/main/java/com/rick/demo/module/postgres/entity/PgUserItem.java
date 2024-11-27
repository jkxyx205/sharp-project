package com.rick.demo.module.postgres.entity;

import com.rick.db.dto.SimpleBaseEntity;
import com.rick.db.plugin.dao.annotation.ManyToOne;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2024/11/27 11:45
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_pg_user_item", comment = "用户信息")
public class PgUserItem extends SimpleBaseEntity<Long> {

    private String name;

    @ManyToOne(value = "t_pg_user_id", parentTable = "t_pg_user")
    private PgUser pgUser;

}
