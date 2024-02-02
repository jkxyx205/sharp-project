package com.rick.admin.sys.role.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.sys.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Rick.Xu
 * @date 2023/5/31 16:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleInfoDTO implements Serializable {

    @JsonProperty("userList")
    private List<User> userList;

    @JsonProperty("permissionList")
    private List<TreeNode> permissionList;
}
