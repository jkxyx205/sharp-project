package com.rick.admin.common.layout;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.formflow.form.controller.instance.PageInstanceController;
import com.rick.formflow.form.service.FormAdvice;
import com.rick.formflow.form.service.FormService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author Rick.Xu
 * @date 2025/6/14 08:57
 */
@Controller
@RequestMapping("forms/page/layout")
public class PageInstanceLayoutController extends PageInstanceController {

    public PageInstanceLayoutController(FormService formService, Map<String, FormAdvice> formAdviceMap) {
        super(formService, formAdviceMap);
    }

    @GetMapping({"{formId}", "{formId}/{instanceId}"})
    public String gotoFormPage(@PathVariable Long formId, @PathVariable(required = false) Long instanceId, Model model, HttpServletRequest request) {
        String page = super.gotoFormPage(formId, instanceId, model, request);
        // page 必须使用 layout
        String reqFormPage = request.getParameter("formPage");
        if (StringUtils.isNotBlank(reqFormPage)) {
            page = reqFormPage;
        }

        String fragment = "content";
        return HttpServletRequestUtils.isAjaxRequest(request) ? page +" :: "+fragment+"" : page;
    }
}
