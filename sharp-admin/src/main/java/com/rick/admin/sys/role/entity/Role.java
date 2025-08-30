package com.rick.admin.sys.role.entity;

import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.user.entity.User;
import com.rick.db.repository.ManyToMany;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseCodeEntity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/27 23:16
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "sys_role", comment = "角色")
public class Role extends BaseCodeEntity<Long> {

    String name;

    @ManyToMany(tableName="sys_role_permission", inverseJoinColumnId = "permission_id", joinColumnId = "role_id")
    List<Permission> permissionList;

    @ManyToMany(tableName="sys_user_role", inverseJoinColumnId = "user_id", joinColumnId = "role_id", cascadeSelect = false)
    List<User> userList;
}