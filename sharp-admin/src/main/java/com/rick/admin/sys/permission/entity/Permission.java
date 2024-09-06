package com.rick.admin.sys.permission.entity;

import com.rick.db.dto.type.BaseCodeEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/**
 * @author Rick.Xu
 * @date 2023/5/27 23:18
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "sys_permission", comment = "权限表")
public class Permission extends BaseCodeEntityWithLongId {

    @Column(comment = "权限名称")
    private String name;

    @Column(comment = "父权限")
    private Long pid;

    @Column(comment = "permission_order")
    private Integer permissionOrder;

}