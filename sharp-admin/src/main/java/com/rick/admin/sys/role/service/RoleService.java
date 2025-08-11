package com.rick.admin.sys.role.service;

import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.sys.permission.service.PermissionService;
import com.rick.admin.sys.role.dao.RoleDAO;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.role.model.RoleInfoDTO;
import com.rick.admin.sys.user.dao.UserDAO;
import com.rick.admin.sys.user.entity.User;
import com.rick.db.plugin.SQLUtils;
import com.rick.db.service.SharpService;
import com.rick.db.service.support.Params;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/31 21:41
 */
@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Validated
public class RoleService {

    RoleDAO roleDAO;

    UserDAO userDAO;

    PermissionService permissionService;

    SharpService sharpService;

    public void removeRoleByUserId(Long roleId, Long userId) {
        SQLUtils.delete("SYS_USER_ROLE", new Object[] {roleId, userId}, "ROLE_ID = ? and USER_ID = ?");
    }

    public List<Role> editRoles(List<Role> roleList) {
        if (CollectionUtils.isEmpty(roleList)) {
            roleDAO.deleteAll();
            SQLUtils.execute("delete from sys_role_permission");
            SQLUtils.execute("delete from sys_user_role");
        } else {
            Set<Long> roleIds = roleList.stream().map(Role::getId).collect(Collectors.toSet());
            SQLUtils.deleteNotIn(roleDAO.getTableName(), "id", roleIds);
            SQLUtils.deleteNotIn("sys_role_permission", "role_id", roleIds);
            SQLUtils.deleteNotIn("sys_user_role", "role_id", roleIds);

            for (Role role : roleList) {
                if (Objects.isNull(role.getId())) {
                    role.setCode(RandomStringUtils.randomAlphanumeric(5));
                    roleDAO.insert(role);
                } else {
                    roleDAO.update("name", Params.builder(1).pv("id", role.getId()).pv("name", role.getName()).build(), "id = :id");
                }
            }
        }

        return roleList;
    }

    public RoleInfoDTO getSettingsInfoByRoleId(Long roleId) {
        List<TreeNode> treeNodeList = permissionService.findTreeNodeByRoleIds(Collections.singletonList(roleId));
        List<Long> userIds = sharpService.query("select user_id from sys_user_role where role_id = :roleId", Params.builder(1).pv("roleId", roleId).build(), Long.class);
        List<User> users;
        if (CollectionUtils.isNotEmpty(userIds)) {
            users = userDAO.selectByParamsWithoutCascade(Params.builder(1).pv("userIds", userIds).build(), "id IN (:userIds)");
        } else {
            users = Collections.emptyList();
        }
        return new RoleInfoDTO(users, treeNodeList);
    }

    public void addPermission(Long roleId, Set<Long> permissionIds) {
        SQLUtils.delete("sys_role_permission", new Object[]{true}, "is_deleted = ?");
        SQLUtils.updateRefTable("sys_role_permission", "role_id", "permission_id", roleId, permissionIds);
    }

    /**
     * 为角色添加用户
     * @param roleId 角色id
     * @param userIds 用户id
     */
    public void addUser(long roleId, Set<Long> userIds) {
        SQLUtils.delete("sys_user_role", new Object[]{true}, "is_deleted = ?");
        SQLUtils.updateRefTable("sys_user_role", "role_id", "user_id", roleId, userIds);
    }
}