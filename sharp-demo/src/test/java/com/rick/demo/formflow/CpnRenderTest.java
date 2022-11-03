package com.rick.demo.formflow;

import com.rick.demo.common.util.ThymeleafRenderHelper;
import com.rick.formflow.form.service.FormService;
import com.rick.formflow.form.service.bo.FormBO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-10-13 15:27:00
 */
@SpringBootTest
public class CpnRenderTest {

    @Autowired
    private FormService formService;

    /**
     * 添加字段
     */
    @Test
    public void testRender() {
        final FormBO.Property property = formService.getFormBOById(487677232379494400L).getPropertyList().get(0);

        // language=HTML
        String html = "<div class=\"mb-3 row\" th:with=\"isRequired = ${p.validatorProperties.get('Required.required') eq null ? false : p.validatorProperties.get('Required.required')}\">\n" +
                "                <label th:for=\"${p.name}\" class=\"col-sm-2 col-form-label\" th:classappend=\"${isRequired ? 'required' : ''}\" th:text=\"${p.configurer.label}\"></label>\n" +
                "                <div class=\"col-sm-10\">\n" +
                "                    <input th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).TEXT}\" type=\"text\" class=\"form-control\" th:id=\"${p.name}\" th:name=\"${p.name}\" th:maxlength=\"${p.validatorProperties.get('Length.max')}\"\n" +
                "                           th:placeholder=\"${p.configurer.placeholder}\" th:value=\"${p.value}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "                    <input th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).NUMBER_TEXT}\" type=\"number\" class=\"form-control\" min=\"0\" th:id=\"${p.name}\" th:name=\"${p.name}\" th:max=\"${p.validatorProperties.get('Size.max')}\" th:min=\"${p.validatorProperties.get('Size.min')}\"\n" +
                "                           th:placeholder=\"${p.configurer.placeholder}\" th:value=\"${p.value}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "                    <input th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).MOBILE}\" type=\"text\" class=\"form-control\" th:id=\"${p.name}\" th:name=\"${p.name}\" maxlength=\"11\"\n" +
                "                           th:placeholder=\"${p.configurer.placeholder}\" th:value=\"${p.value}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "                    <textarea th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).TEXTAREA}\" type=\"number\" class=\"form-control\" th:id=\"${p.name}\" th:name=\"${p.name}\" th:maxlength=\"${p.validatorProperties.get('Length.max')}\"\n" +
                "                              th:placeholder=\"${p.configurer.placeholder}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\" th:text=\"${p.value}\"></textarea>\n" +
                "                    <select th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).SELECT}\" class=\"form-control\" th:id=\"${p.name}\" th:name=\"${p.name}\"\n" +
                "                           th:placeholder=\"${p.configurer.placeholder}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "                        <option th:each=\"option : ${p.configurer.options}\" th:value=\"${option}\" th:text=\"${option}\" th:selected=\"${option eq p.value}\"></option>\n" +
                "                    </select>\n" +
                "                    <div th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).FILE}\">\n" +
                "                        <input type=\"file\" class=\"form-control\" th:id=\"${p.name} + '_file'\" th:name=\"${p.name} + '_file'\" th:onchange=\"ajaxFileUpload([[${p.name}+ '_file']], [[${p.name}]])\"\n" +
                "                           th:placeholder=\"${p.conf" +
                "igurer.placeholder}\" multiple>\n" +
                "                        <input type=\"text\" style=\"display:none;\" th:name=\"${p.name}\" th:id=\"${p.name}\" th:value=\"${p.value ne null ? T(com.rick.common.util.JsonUtils).toJson(p.value) : ''}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "                        <!--前端验证所有验证信息-->\n" +
                "                        <div class=\"invalid-feedback\" th:each=\"v : ${p.configurer.validatorList}\"\n" +
                "                             th:text=\"${v.message}\"></div>\n" +
                "                            <ul class=\"list-group list-group-flush\">\n" +
                "                                <th:block th:if=\"${p.value ne null}\">\n" +
                "                                <li class=\"list-group-item\" th:each=\"f : ${p.value}\">\n" +
                "                                    <a th:text=\"${f.fullName}\" th:href=\"${f.url}\" target=\"_blank\"></a><button type=\"button\" class=\"btn btn-link\" th:onclick=\"deleteAttachment([[${p.name}]], [[${f.id}]], this)\">删除</button>\n" +
                "                                </li>\n" +
                "                                </th:block>\n" +
                "                            </ul>\n" +
                "                    </div>\n" +
                "                    <input th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).EMAIL}\" type=\"email\" class=\"form-control\" th:id=\"${p.name}\" th:name=\"${p.name}\" th:maxlength=\"${p.validatorProperties.get('Length.max')}\"\n" +
                "                           th:placeholder=\"${p.configurer.placeholder}\" th:value=\"${p.value}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "\n" +
                "                    <input th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).DATE}\" type=\"date\" class=\"form-control\" th:id=\"${p.name}\" th:name=\"${p.name}\"\n" +
                "                           th:placeholder=\"${p.configurer.placeholder}\" th:value=\"${p.value}\" th:required=\"${isRequired}\" th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "\n" +
                "                    <div class=\"form-check\" th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).CHECKBOX}\" th:each=\"option : ${p.configurer.options}\">\n" +
                "                        <input class=\"form-check-input\" type=\"checkbox\" th:name=\"${p.name}\" th:id=\"${option}\" th:value=\"${option}\" th:checked=\"${p.value ne null && #sets.contains(p.value, option)}\" th:required=\"${p.configurer.options.length eq 1 && isRequired}\"\n" +
                "                               th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "                        <label class=\"form-check-label\" th:for=\"${option}\" th:text=\"${option}\"></label>\n" +
                "                        <th:block th:if=\"${p.configurer.options.length eq 1}\">\n" +
                "                            <!--前端验证所有验证信息-->\n" +
                "                            <div class=\"invalid-feedback\" th:each=\"v : ${p.configurer.validatorList}\"\n" +
                "                                 th:text=\"${v.message}\"></div>\n" +
                "                        </th:block>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <div class=\"form-check\" th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).RADIO}\" th:each=\"option : ${p.configurer.options}\">\n" +
                "                        <input class=\"form-check-input\" type=\"radio\" th:name=\"${p.name}\" th:id=\"${option}\" th:value=\"${option}\" th:checked=\"${p.value ne null && #sets.contains(p.value, option)}\"\n" +
                "                               th:classappend=\"${errors eq null ? '' : (#maps.containsKey(errors, p.name) ? 'is-invalid' : 'is-valid')}\">\n" +
                "                        <label class=\"form-check-label\" th:for=\"${option}\" th:text=\"${option}\"></label>\n" +
                "                    </div>\n" +
                "\n" +
                "                    <table class=\"table\"\n" +
                "                           th:if=\"${p.configurer.cpnType == T(com.rick.formflow.form.cpn.core.CpnTypeEnum).TABLE}\">\n" +
                "                        <thead>\n" +
                "                        <tr>\n" +
                "                            <th th:each=\"label : ${p.configurer.getAdditionalInfo().get('labels')}\" th:text=\"${label}\"></th>\n" +
                "                        </tr>\n" +
                "                        </thead>\n" +
                "                        <tbody>\n" +
                "                        <tr th:each=\"row : ${p.value}\">\n" +
                "                            <td th:each=\"col: ${row}\" th:text=\"${col}\"></td>\n" +
                "                        </tr>\n" +
                "                        </tbody>\n" +
                "                    </table>\n" +
                "                    <th:block th:if=\"${p.configurer.cpnType ne T(com.rick.formflow.form.cpn.core.CpnTypeEnum).CHECKBOX &&\n" +
                "                    p.configurer.cpnType ne T(com.rick.formflow.form.cpn.core.CpnTypeEnum).FILE}\">\n" +
                "                        <div class=\"invalid-feedback\" th:each=\"v : ${p.configurer.validatorList}\"\n" +
                "                             th:text=\"${v.message}\"></div>\n" +
                "                    </th:block>\n" +
                "                </div>\n" +
                "            </div>";

        Map<String, Object> params = new HashMap<>();
        params.put("p", property);
        String htmlContent = ThymeleafRenderHelper.renderByHtmlContent(html, params);
        System.out.println(htmlContent);
    }


}
