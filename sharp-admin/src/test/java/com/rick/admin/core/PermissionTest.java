package com.rick.admin.core;

import com.google.common.collect.Sets;
import com.rick.admin.sys.permission.dao.PermissionDAO;
import com.rick.admin.sys.permission.entity.Permission;
import com.rick.admin.sys.role.service.RoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:41
 */
@SpringBootTest
public class PermissionTest {

    @Autowired
    private PermissionDAO permissionDAO;

    @Autowired
    private RoleService roleService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建目录
     */
    @Test
    public void testAddPermission0() {
        permissionDAO.insertOrUpdate(Permission.builder()
                .code("manager")
                .name("管理")
//                .pid(708744048906604544L)
                .permissionOrder(0)
                .build());
    }

    /**
     * 表权限
     */
    @Test
    public void testAddPermission1() {
        final String CODE = "t_supplier";
        final String NAME = "供应商管理";
        final Long PARENT_ID = 866065023858941952L;
        final int PERMISSION_ORDER = 1;
        testAddPermission(CODE, NAME, PARENT_ID, PERMISSION_ORDER);
    }
    @Test
    public void testAddPermission(String CODE, String NAME, Long PARENT_ID, int PERMISSION_ORDER) {
        jdbcTemplate.execute("delete from sys_role_permission where permission_id IN (select id from sys_permission where code like'"+CODE+"%')");

        jdbcTemplate.execute("delete from sys_permission where code like'"+CODE+"%'");

        Permission root = Permission.builder()
                .code(CODE)
                .name(NAME)
                .pid(PARENT_ID)
                .permissionOrder(PERMISSION_ORDER)
                .build();

        permissionDAO.insertOrUpdate(root);

        Permission read = Permission.builder()
                .code(CODE + "_read")
                .name("查看")
                .pid(root.getId())
                .permissionOrder(0)
                .build();

        Permission add = Permission.builder()
                .code(CODE + "_add")
                .name("新增")
                .pid(root.getId())
                .permissionOrder(1)
                .build();

        Permission edit = Permission.builder()
                .code(CODE + "_edit")
                .name("编辑")
                .pid(root.getId())
                .permissionOrder(2)
                .build();

        Permission delete = Permission.builder()
                .code(CODE + "_delete")
                .name("删除")
                .pid(root.getId())
                .permissionOrder(3)
                .build();

        permissionDAO.insertOrUpdate(read);
        permissionDAO.insertOrUpdate(add);
        permissionDAO.insertOrUpdate(edit);
        permissionDAO.insertOrUpdate(delete);

        // 管理员分配权限
        List<Long> permissionIds = permissionDAO.selectByParams(null, "id", Long.class);
        roleService.addPermission(694587732420202496L, Sets.newHashSet(permissionIds));
    }
}
