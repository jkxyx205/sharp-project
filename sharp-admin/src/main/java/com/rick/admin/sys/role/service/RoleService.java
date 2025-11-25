package com.rick.admin.sys.role.service;

import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.sys.permission.service.PermissionService;
import com.rick.admin.sys.role.dao.RoleDAO;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.role.model.RoleInfoDTO;
import com.rick.admin.sys.user.dao.UserDAO;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.util.Maps;
import com.rick.db.repository.TableDAO;
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

    TableDAO tableDAO;

    public void removeRoleByUserId(Long roleId, Long userId) {
        tableDAO.delete("SYS_USER_ROLE", "ROLE_ID = ? and USER_ID = ?", new Object[] {roleId, userId});
    }

    public List<Role> editRoles(List<Role> roleList) {
        if (CollectionUtils.isEmpty(roleList)) {
            roleDAO.deleteAll();
            tableDAO.execute("delete from sys_role_permission");
            tableDAO.execute("delete from sys_user_role");
        } else {
            Set<Long> roleIds = roleList.stream().map(Role::getId).collect(Collectors.toSet());
            tableDAO.deleteNotIn(roleDAO.getTableMeta().getTableName(), "id", roleIds);
            tableDAO.deleteNotIn("sys_role_permission", "role_id", roleIds);
            tableDAO.deleteNotIn("sys_user_role", "role_id", roleIds);


            for (Role role : roleList) {
                if (Objects.isNull(role.getId())) {
                    role.setCode(RandomStringUtils.randomAlphanumeric(5));
                    roleDAO.insert(role);
                } else {
                    roleDAO.update("name", "id = :id", Maps.of("id", role.getId(),"name", role.getName()));
                }
            }
        }

        return roleList;
    }

    public RoleInfoDTO getSettingsInfoByRoleId(Long roleId) {
        List<TreeNode> treeNodeList = permissionService.findTreeNodeByRoleIds(Collections.singletonList(roleId));
        List<Long> userIds = tableDAO.select(Long.class, "select user_id from sys_user_role where role_id = :roleId", Maps.of("roleId", roleId));
        List<User> users;
        if (CollectionUtils.isNotEmpty(userIds)) {
            users = userDAO.selectWithoutCascade(User.class, userDAO.getTableMeta().getSelectColumn(), "id IN (:userIds)", Maps.of("userIds", userIds));
        } else {
            users = Collections.emptyList();
        }
        return new RoleInfoDTO(users, treeNodeList);
    }

    public void addPermission(Long roleId, Set<Long> permissionIds) {
        tableDAO.delete("sys_role_permission", "is_deleted = ?", new Object[]{true});
        tableDAO.updateRefTable("sys_role_permission", "role_id", "permission_id", roleId, permissionIds);
    }

    /**
     * 为角色添加用户
     * @param roleId 角色id
     * @param userIds 用户id
     */
    public void addUser(long roleId, Set<Long> userIds) {
        tableDAO.delete("sys_user_role", "is_deleted = ?", new Object[]{true});
        tableDAO.updateRefTable("sys_user_role", "role_id", "user_id", roleId, userIds);
    }
}