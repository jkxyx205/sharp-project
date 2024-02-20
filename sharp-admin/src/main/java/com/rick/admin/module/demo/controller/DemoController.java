package com.rick.admin.module.demo.controller;

import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.plugin.ztree.model.TreeNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Rick.Xu
 * @date 2024/2/5 21:51
 */
@Controller
@RequestMapping("demos")
@RequiredArgsConstructor
public class DemoController {

    private final TreeNodeService treeNodeService;

    @GetMapping("dialog-report-picker")
    public String testDialogReportPicker() {
        return "demos/dialogReportPicker";
    }

    @GetMapping("table-plus")
    public String testTablePlus() {
        return "demos/tablePlus";
    }

    @GetMapping("ztree")
    public String ztree() {
        return "demos/ztree";
    }

    @GetMapping("ztree/url")
    @ResponseBody
    public List<TreeNode> ztreeUrl() {
        return treeNodeService.getTreeNode("SELECT p.id as \"id\", p.name as \"name\", pid as \"pId\", 1 as open\n" +
                "   FROM sys_permission p where is_deleted = 0 \n" +
                "  order by p.permission_order asc", null);
    }
}
