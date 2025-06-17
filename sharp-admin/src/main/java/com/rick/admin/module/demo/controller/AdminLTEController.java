package com.rick.admin.module.demo.controller;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.exception.BizException;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Rick.Xu
 * @date 2024/2/5 21:51
 */
@Controller
@RequestMapping("adminlte")
@RequiredArgsConstructor
public class AdminLTEController {

    @GetMapping
    public String index() {
        return "redirect:/adminlte/index";
    }

    @GetMapping("index")
    public String index(HttpServletRequest request) {
        return page(request, "adminlte/index");
    }


    @GetMapping("demo")
    public String demo(HttpServletRequest request) {
        return page(request, "adminlte/demo");
     }

    @GetMapping("about")
    public String about(HttpServletRequest request) {
        return page(request, "adminlte/about");
    }

    @GetMapping("index/tab-a")
    public String indexTabA(HttpServletRequest request, Model model) {
        model.addAttribute("tab", "tab-a");
        return page(request, "adminlte/index-tab-a", "content-content");
    }

    @GetMapping("index/tab-b")
    public String indexTabB(HttpServletRequest request, Model model) {
        model.addAttribute("tab", "tab-b");
        return page(request, "adminlte/index-tab-b", "content-content");
    }

    @GetMapping("detail/{code}")
    public String detail(HttpServletRequest request, @PathVariable String code) {
        request.setAttribute("code", code);
        return page(request, "adminlte/detail");
    }

    @GetMapping("exception")
    @ResponseBody
    public void exception() {
        throw new BizException("业务异常");
    }

    @GetMapping("exception2")
    @ResponseBody
    public Result exception2() {
        return ResultUtils.fail("手机号不能为空");
    }

    @GetMapping("exception3")
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
