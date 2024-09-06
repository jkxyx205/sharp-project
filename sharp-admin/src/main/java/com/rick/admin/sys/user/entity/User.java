package com.rick.admin.sys.user.entity;

import com.rick.admin.sys.role.entity.Role;
import com.rick.db.dto.type.BaseCodeEntityWithLongId;
import com.rick.db.plugin.dao.annotation.Column;
import com.rick.db.plugin.dao.annotation.ManyToMany;
import com.rick.db.plugin.dao.annotation.Table;
import com.rick.db.plugin.dao.annotation.Transient;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rick.Xu
 * @date 2023/5/27 18:45
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
@Table(value = "sys_user", comment = "用户信息")
public class User extends BaseCodeEntityWithLongId {

    @Column(comment = "姓名")
    String name;

    @Column(comment = "密码", columnDefinition = "varchar(128)", updatable = false)
    String password;

    LocalDate birthday;

    @Column(value = "is_available", comment = "是否可用")
    Boolean available;

    @ManyToMany(thirdPartyTable="sys_user_role", referenceTable="sys_role", referenceColumnName = "role_id", columnDefinition = "user_id")
    List<Role> roleList;

    /**
     * noted: sharp form 控件file 只能用 List<Map> 不能使用List<Document>
     */
    @Column(value = "attachment", comment = "附件")
    List<Map> documentList;

    /**
     * 权限列表
     */
    @Transient
    List<String> authorityList;

    @Transient
    private String imgName;

//    @Transient
//    List<Long> roleIds;

    /**
     * 计算属性，映射到 form 的字段 roleIds
     * @return
     */
    public List<String> getRoleIds() {
        if (roleList == null) {
            return Collections.emptyList();
        }

        return roleList.stream().map(role -> String.valueOf(role.getId())).collect(Collectors.toList());
    }
}