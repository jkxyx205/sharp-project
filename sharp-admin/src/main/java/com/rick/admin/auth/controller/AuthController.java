package com.rick.admin.auth.controller;

import com.rick.admin.auth.authentication.AdminUserDetails;
import com.rick.admin.auth.common.AuthConstants;
import com.rick.admin.common.exception.ExceptionCodeEnum;
import com.rick.admin.common.layout.LayoutUtils;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.plugin.ztree.model.TreeNodeService;
import com.rick.admin.sys.permission.model.UserPermissionVO;
import com.rick.admin.sys.permission.service.PermissionService;
import com.rick.admin.sys.role.dao.RoleDAO;
import com.rick.admin.sys.role.entity.Role;
import com.rick.admin.sys.role.model.RoleInfoDTO;
import com.rick.admin.sys.role.service.RoleService;
import com.rick.admin.sys.user.dao.UserDAO;
import com.rick.admin.sys.user.entity.User;
import com.rick.admin.sys.user.service.UserService;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.meta.dict.service.DictService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author rick
 */
@Controller
@RequestMapping
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    private final UserDAO userDAO;

    private final RoleDAO roleDAO;
    
    private final TreeNodeService treeNodeService;

    private final PermissionService permissionService;

    private final RoleService roleService;

    private final DictService dictService;

    /**
     * login页面
     */
    @GetMapping("login")
    public String login(Principal principal) {
        if (Objects.nonNull(principal)) {
            return "redirect:/";
        }
        return "login";
    }

    @DeleteMapping("users/{id}")
    @ResponseBody
    public Result<Integer> deleteUser(@PathVariable Long id) {
        return ResultUtils.success(userDAO.deleteLogicallyById(id));
    }

    /**
     * 修改密码
     */
    @PostMapping("password/update")
    @ResponseBody
    public Result<?> updatePassword(@RequestBody User user, Principal principal) {
        Long userId = ((AdminUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser().getId();
        userService.updatePassword(userId, user.getPassword());
        return ResultUtils.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("password/{userId}/reset")
    @PreAuthorize("hasAuthority('sys_user')")
    @ResponseBody
    public Result<String> updatePassword(@PathVariable Long userId) {
        String newPassword = AuthConstants.DEFAULT_PASSWORD;
        userService.updatePassword(userId, newPassword);
        return ResultUtils.success(newPassword);
    }

    /**
     * 原密码验证
     */
    @GetMapping("password/check")
    @ResponseBody
    public boolean checkPassword(String password, Principal principal) {
        Long userId = ((AdminUserDetails) ((UsernamePasswordAuthenticationToken) principal).getPrincipal()).getUser().getId();
        return userService.checkPassword(userId, password);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('role_management')")
    public String gotoAuthPage(Model model) {
        List<Role> roleList = roleDAO.selectAll();
        List<User> userList = userDAO.selectAll();

        Long roleId = roleList.get(0).getId();
        RoleInfoDTO roleInfo = roleService.getSettingsInfoByRoleId(roleId);

        model.addAttribute("roleList", roleList);
        model.addAttribute("userList", userList);
        model.addAttribute("roleInfo", roleInfo);
        model.addAttribute("userIds", roleInfo.getUserList().stream().map(user -> String.valueOf(user.getId())).collect(Collectors.toList()));

        // language=SQL
        model.addAttribute("permissionFullList", treeNodeService.getTreeNode("SELECT p.id as \"id\", p.name as \"name\", pid as \"pId\", 1 as open\n" +
                "   FROM sys_permission p where is_deleted = FALSE \n" +
                "  order by p.permission_order asc", null));

        return "sys/auth";
    }

    @GetMapping("/roles/adminlte")
    @PreAuthorize("hasAuthority('role_management')")
    public String gotoAuthPage(Model model, HttpServletRequest request) {
        gotoAuthPage(model);
        return LayoutUtils.page(request, "adminlte/auth");
    }

    @GetMapping("/auth/{roleId}/info")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<RoleInfoDTO> getPermissionNodesByRoleId(@PathVariable Long roleId) {
        return ResultUtils.success(roleService.getSettingsInfoByRoleId(roleId));
    }

    @GetMapping("/auth/permission")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<UserPermissionVO> getPermissionNodesByUserId(Long userId) {
        Optional<User> optional = userDAO.selectById(userId);

        User user = optional.orElseThrow(() -> new BizException(ExceptionCodeEnum.USER_NOT_FOUND));

        List<TreeNode> treeNodeList = permissionService.findTreeNodeByRoleIds(user.getRoleIds().stream().map(Long::parseLong).collect(Collectors.toSet()));

        UserPermissionVO userPermissionVO = UserPermissionVO.builder().permissionList(treeNodeList)
                .roleList(user.getRoleList()).build();

        return ResultUtils.success(userPermissionVO);
    }

    /**
     * 用户保存
     * @param roleId 角色id
     * @param userIds 用户id
     */
    @PostMapping("/auth/assign/role/{roleId}/user")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<?> assignAuthUser(@PathVariable Long roleId, @RequestParam(value = "userIds[]", required = false) Set<Long> userIds) {
        roleService.addUser(roleId, userIds);
        return ResultUtils.success();
    }

    /**
     * 权限保存
     * @param roleId 角色id
     * @param permissionIds 权限ids
     */
    @PostMapping("/auth/assign/role/{roleId}/permission")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<?> assignAuthPermission(@PathVariable Long roleId, @RequestParam(value = "permissionIds[]", required = false) Set<Long> permissionIds) {
        roleService.addPermission(roleId, permissionIds);
        return ResultUtils.success();
    }

    /**
     * 角色保存
     * @param roleList 角色列表
     */
    @PostMapping("/auth/add/roles")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<List<Role>> editAuthRole(@RequestBody List<Role> roleList) {
        Result<List<Role>> success = ResultUtils.success(roleService.editRoles(roleList));
        dictService.rebuild("sys_role");
        return success;
    }

    /**
     * 移除用户角色
     * @param roleId 角色id
     * @param userId 用户id
     */
    @PostMapping("/auth/assign/role/{roleId}/{userId}")
    @ResponseBody
    @PreAuthorize("hasAuthority('role_management')")
    public Result<?> removeUserRoleAuth(@PathVariable Long roleId, @PathVariable Long userId) {
        roleService.removeRoleByUserId(roleId, userId);
        return ResultUtils.success();
    }

}
