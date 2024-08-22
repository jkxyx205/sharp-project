package com.rick.admin.module.demo.controller;

import com.google.common.collect.Sets;
import com.rick.admin.module.demo.entity.ComplexModel;
import com.rick.admin.module.demo.model.EmbeddedValue;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.plugin.ztree.model.TreeNodeService;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * @author Rick.Xu
 * @date 2024/2/5 21:51
 */
@Controller
@RequestMapping("demos")
@RequiredArgsConstructor
public class DemoController {

    private final TreeNodeService treeNodeService;

    private final EntityDAO<ComplexModel, Long> complexModelDAO;

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

    @GetMapping("dict")
    public String dicts(Model model) {
        model.addAttribute("unit", "EA");
        model.addAttribute("category", "PACKAGING");
        model.addAttribute("material", "M1");
        model.addAttribute("category2", Sets.newHashSet("SALES_ORG", "MATERIAL", "PACKAGING"));
        model.addAttribute("name", "1");

        // 字典值
        model.addAttribute("CategoryEnum", DictUtils.getDict("CategoryEnum"));
        model.addAttribute("MATERIAL", DictUtils.getDict("MATERIAL"));
        return "demos/dict";
    }

    @GetMapping("complex_models")
    @ResponseBody
    public Result getData() {
        Optional<ComplexModel> optional = complexModelDAO.selectById(856759492044419072L);
        ComplexModel complexModel = optional.get();
        System.out.println(complexModel);
        // 获取label
        complexModel.setEmbeddedValue(new EmbeddedValue(new DictValue("HIBE"), "text"));
        DictUtils.fillDictLabel(complexModel);
        System.out.println(complexModel);
        return ResultUtils.success(complexModel);
    }

    @GetMapping("complex_models/{id}")
    @ResponseBody
    public Result getDataById(@PathVariable Long id) {
        Optional<ComplexModel> optional = complexModelDAO.selectById(id);
        ComplexModel complexModel = optional.get();
        DictUtils.fillDictLabel(complexModel);
        return ResultUtils.success(complexModel);
    }

    @PermitAll // 无效， 配置文件WebSecurityConfig 需要认证.anyRequest().authenticated()
    @PostMapping("complex_models")
    @ResponseBody
    public Result saveData(@RequestBody @Valid ComplexModel complexModel) {
        complexModelDAO.insert(complexModel);
        return ResultUtils.success(complexModel.getId());
    }

    @GetMapping("markdown")
    public String markdown(Model model) {
        model.addAttribute("markdown", "# hello\n" +
                "## word\n" +
                "```javascript\n" +
                "function send() {\n" +
                "}\n" +
                "```\n" +
                "`java`\n" +
                "**haha**");
        return "demos/markdown";
    }
}
