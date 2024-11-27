package com.rick.demo.module.postgres.entity;

import com.rick.db.dto.SimpleEntity;
import com.rick.db.plugin.dao.annotation.OneToMany;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/11/27 11:45
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "t_pg_user", comment = "用户信息")
public class PgUser extends SimpleEntity<Long> {

    private String name;

    private Short age;

    @OneToMany(subTable = "t_pg_user_item", reversePropertyName = "pgUser", cascadeInsertOrUpdate = true, joinValue = "t_pg_user_id")
    private List<PgUserItem> pgUserItemList;

}
