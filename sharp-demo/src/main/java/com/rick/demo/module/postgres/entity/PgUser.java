package com.rick.demo.module.postgres.entity;

import com.rick.db.repository.OneToMany;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.EntityId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

;

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
public class PgUser extends EntityId<Long> {

    private String name;

    private Short age;

    @OneToMany(mappedBy = "pgUser", joinColumnId = "t_pg_user_id")
    private List<PgUserItem> pgUserItemList;

}
