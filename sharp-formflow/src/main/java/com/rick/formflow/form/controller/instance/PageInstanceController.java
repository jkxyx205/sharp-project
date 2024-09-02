package com.rick.formflow.form.controller.instance;

import com.google.common.collect.Maps;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.util.JsonUtils;
import com.rick.formflow.form.cpn.core.CpnConfigurer;
import com.rick.formflow.form.cpn.core.CpnTypeEnum;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.service.bo.FormBO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rick
 * @createdAt 2021-11-04 17:00:00
 */
@Controller
@RequestMapping("forms/page")
@RequiredArgsConstructor
public class PageInstanceController {

    private final FormService formService;

    /**
     * http://localhost:8080/form/487677232379494400/487684156282011648
     * @return
     */
    @GetMapping({"{formId}", "{formId}/{instanceId}"})
    public String gotoFormPage(@PathVariable Long formId, @PathVariable(required = false) Long instanceId, Model model, HttpServletRequest request) {
        FormBO formBO = formService.getFormBO(formId, instanceId);

        model.addAttribute("formBO", formBO);
        model.addAttribute("model", getDataModel(formBO.getPropertyList()));
        model.addAttribute("query", HttpServletRequestUtils.getParameterMap(request));

        // table
        for (FormBO.Property property : formBO.getPropertyList()) {
            if (property.getConfigurer().getCpnType() == CpnTypeEnum.TABLE) {
                List<Map<String, Object>> list = (List<Map<String, Object>>) property.getConfigurer().getAdditionalInfo().get("columns");
                List<CpnConfigurer> tableCpnConfigurerList = new ArrayList<>();
                for (Map<String, Object> map : list) {
                    CpnConfigurer cpnConfigurer = JsonUtils.toObject(JsonUtils.toJson(map), CpnConfigurer.class);
                    tableCpnConfigurerList.add(cpnConfigurer);
                }
                property.getConfigurer().getAdditionalInfo().put("columns", tableCpnConfigurerList);
            }
        }

        return StringUtils.defaultString(formBO.getForm().getTplName(),"form");
    }

    @PostMapping( {"{formId}/{instanceId}", "{formId}"})
    public String saveOrUpdate(HttpServletRequest request, @PathVariable Long formId, @PathVariable(required = false) Long instanceId, Model model) {
        Map<String, Object> values = HttpServletRequestUtils.getParameterMap(request);
        try {
            if (Objects.nonNull(instanceId)) {
                formService.post(formId, instanceId, values);
            } else {
                formService.post(formId, values);
            }
            return "success";
        } catch (BindException e) {
            model.addAttribute("errors", e.getAllErrors().stream().collect(Collectors.toMap(o -> ((FieldError)o).getField(), o -> o)));
            FormBO formBO = formService.getFormBO(formId, instanceId);
            model.addAttribute("formBO", formBO);
            // 填充表单数据
            for (FormBO.Property property : formBO.getPropertyList()) {
                property.setValue(values.get(property.getName()));
            }

            model.addAttribute("model", getDataModel(formBO.getPropertyList()));
            return StringUtils.defaultString(formBO.getForm().getTplName(),"form");
        }
    }

    private Map<String, Object> getDataModel(List<FormBO.Property> propertyList) {
        Map<String, Object> dataModel = Maps.newLinkedHashMapWithExpectedSize(propertyList.size());
        for (FormBO.Property property : propertyList) {
            dataModel.put(property.getName(), property.getValue());
        }
        return dataModel;
    }

}
