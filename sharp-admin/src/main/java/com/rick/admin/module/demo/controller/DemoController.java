package com.rick.admin.module.demo.controller;

import com.google.common.collect.Sets;
import com.rick.admin.module.demo.entity.ComplexModel;
import com.rick.admin.module.demo.model.EmbeddedValue;
import com.rick.admin.plugin.ztree.model.TreeNode;
import com.rick.admin.plugin.ztree.model.TreeNodeService;
import com.rick.admin.sys.user.entity.User;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.exception.BizException;
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
import javax.servlet.http.HttpServletRequest;
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

    @GetMapping("code-input")
    public String codeInput(Model model) {
        return "demos/code-input";
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
                "**haha**" +
                "" +
                "![图片](http://localhost:7892/link/858375629681201152.png)");
        return "demos/markdown";
    }

    @GetMapping("ckeditor")
    public String ckeditor(Model model) {
        model.addAttribute("ckeditor", "<div class=\"ckeditor-html5-video\" style=\"text-align: center;\">\n" +
                "<video controls=\"controls\" controlslist=\"nodownload\" poster=\"http://cimc.cncdh2.com/_nuxt/img/3.99045f8.jpg\" src=\"http://cdn.devyean.com/ch2/videos/2.mp4\" width=\"60%\"><source src=\"http://cdn.devyean.com/ch2/videos/2.mp4\" type=\"video/mp4\" /></video>\n" +
                "</div>\n" +
                "\n" +
                "<p><img alt=\"\" height=\"1450\" src=\"http://localhost:7892/ckeditor/858097155238309888.png\" width=\"2050\" /></p>\n" +
                "\n" +
                "<p><span><img alt=\"\" src=\"/plugins/ckeditor/plugins/file/icons/word.png\" style=\"vertical-align: middle; width: 16px;\" />&nbsp;&nbsp;<a href=\"http://localhost:7892/ckeditor/858143002420359168.docx\" target=\"_blank\">糖尿病肾病 试运行.docx</a>&nbsp;<span style=\"color:#BCBCBC;\">13.22K</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span><br />\n" +
                "<span><img alt=\"\" src=\"/plugins/ckeditor/plugins/file/icons/image.png\" style=\"vertical-align: middle; width: 16px;\" />&nbsp;&nbsp;<a href=\"http://localhost:7892/ckeditor/858143015569502208.png\" target=\"_blank\">点研营业执照new.png</a>&nbsp;<span style=\"color:#BCBCBC;\">2.12M</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span><br />\n" +
                "<span><img alt=\"\" src=\"/plugins/ckeditor/plugins/file/icons/else.png\" style=\"vertical-align: middle; width: 16px;\" />&nbsp;&nbsp;<a href=\"http://localhost:7892/ckeditor/858143018471960576\" target=\"_blank\">note</a>&nbsp;<span style=\"color:#BCBCBC;\">0.03K</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span><br />\n" +
                "<span><img alt=\"\" src=\"/plugins/ckeditor/plugins/file/icons/pdf.png\" style=\"vertical-align: middle; width: 16px;\" />&nbsp;&nbsp;<a href=\"http://localhost:7892/ckeditor/858143021236006912.pdf\" target=\"_blank\">投资任职情况查询报告.pdf</a>&nbsp;<span style=\"color:#BCBCBC;\">34.52K</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span></p>\n" +
                "\n" +
                "<p><span style=\"color:#e74c3c;\"><strong>hello world</strong></span></p>");
        return "demos/ckeditor";
    }

    @GetMapping("ajax-table")
    public String ajaxTable() {
        return "demos/ajax-table";
    }

    @GetMapping("jq-valid")
    public String jqValid() {
        return "demos/jq-valid";
    }

    @GetMapping("alpine-js")
    public String AlpineJs(Model model) {
        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);
        return "demos/alpine-js";
    }


    @GetMapping("htmx")
    public String htmx(Model model) {
        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);
        return "redirect:/demos/htmx/index";
    }


    @GetMapping("htmx/demo")
    public String htmxDemo(Model model, HttpServletRequest request) {
        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);
        return page(request, "demos/htmx/demo");
     }


    @GetMapping("htmx/index")
    public String htmxIndex(Model model, HttpServletRequest request) {
        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);

        return page(request, "demos/htmx/index");
    }

    @GetMapping("htmx/about")
    public String htmxAbout(Model model, HttpServletRequest request) {
        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);

        return page(request, "demos/htmx/about");
    }

    @GetMapping("htmx/index/tab-a")
    public String htmxIndexTabA(Model model, HttpServletRequest request) {
        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);
        model.addAttribute("tab", "tab-a");
        // 一级路由用 fragment=content 二级路由用 content-content，三级路由用 content-content-content，以此类推
        return page(request, "demos/htmx/index-tab-a", "content-content");
    }

    @GetMapping("htmx/index/tab-b")
    public String htmxIndexTabB(Model model, HttpServletRequest request) {
        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);
        model.addAttribute("tab", "tab-b");
        return page(request, "demos/htmx/index-tab-b", "content-content");
    }

    @GetMapping("htmx/detail/{code}")
    public String detail(Model model, HttpServletRequest request, @PathVariable String code) {
        model.addAttribute("code", code);

        User user = new User();
        user.setCode("0000001");
        user.setName("Rick.Xu");

        model.addAttribute("user", user);
        return page(request, "demos/htmx/detail");
    }

    @GetMapping("htmx/exception")
    @ResponseBody
    public void exception() {
        throw new BizException("业务异常");
    }

    @GetMapping("htmx/exception2")
    @ResponseBody
    public Result exception2() {
        return ResultUtils.fail("手机号不能为空");
    }

    @GetMapping("htmx/exception3")
    @ResponseBody
    public Result exception3() {
        int a = 500 / 0;
        return ResultUtils.success();
    }

    private String page(HttpServletRequest request, String page) {
        return page(request, page, "content");
    }

    private String page(HttpServletRequest request, String page, String fragment) {
        return HttpServletRequestUtils.isAjaxRequest(request) ? page +" :: "+fragment+"" : page;
    }

}
