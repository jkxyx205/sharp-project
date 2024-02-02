package com.rick.admin.sys.role.entity;

import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.user.entity.User;
import com.rick.db.dto.BaseCodeEntity;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.Table;
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
public class Role extends BaseCodeEntity {

    String name;

    @ManyToMany(thirdPartyTable="sys_role_permission", referenceTable="sys_permission", referenceColumnName = "permission_id", columnDefinition = "role_id")
    List<Permission> permissionList;

    @ManyToMany(thirdPartyTable="sys_user_role", referenceTable="sys_user", referenceColumnName = "user_id", columnDefinition = "role_id")
    List<User> userList;
}