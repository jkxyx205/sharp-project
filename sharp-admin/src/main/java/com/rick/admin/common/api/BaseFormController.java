package com.rick.admin.common.api;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.HtmlTagUtils;
import com.rick.db.constant.SharpDbConstants;
import com.rick.db.dto.BaseEntity;
import com.rick.db.plugin.dao.core.EntityDAO;
import com.rick.db.plugin.dao.core.EntityDAOManager;
import com.rick.db.service.BaseServiceImpl;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;

/**
 * 表单的增删改
 * @author Rick.Xu
 * @date 2024/1/26 16:05
 */
public class BaseFormController<E extends BaseEntity, S extends BaseServiceImpl> {

    protected final S baseService;

    private final String formPage;

    private final String entityPropertyName;

    private final Class entityClass;

    public BaseFormController(S service, String formPage) {
        this.baseService = service;
        this.formPage = formPage;
        this.entityClass = ClassUtils.getClassGenericsTypes(this.getClass())[0];
        String simpleName = entityClass.getSimpleName();
        this.entityPropertyName = simpleName.substring(0, 1).toLowerCase() + simpleName.substring(1);
    }

    @GetMapping("new")
    public String gotoFormPage(HttpServletRequest request, Model model) {
        Object entity = BeanUtils.instantiateClass(this.entityClass);

        Field[] fields = ClassUtils.getAllFields(entityClass);
        for (Field field : fields) {
            if (List.class == field.getType()) {
                ClassUtils.setFieldValue(entity, field, Collections.emptyList());
            } if (Set.class == field.getType()) {
                ClassUtils.setFieldValue(entity, field, Collections.emptySet());
            }
        }

        model.addAttribute(entityPropertyName, entity);
        model.addAttribute("query", HttpServletRequestUtils.getParameterMap(request));

        addAttributeOfDict(model);
        return this.formPage;
    }

    @GetMapping("{id}/page")
    public String gotoFormPageById(HttpServletRequest request, @PathVariable Long id, Model model) {
        Map<String, String> parameterMap = HttpServletRequestUtils.getParameterStringMap(request);

        Optional<E> op = baseService.findById(id);
        Object entity = op.orElseThrow(() -> new ResourceNotFoundException());
        DictUtils.fillDictLabel(entity); // 可选操作
        model.addAttribute(entityPropertyName, entity);

        model.addAttribute("query", parameterMap);
        // 这个是必须的字段，用于界面显示
        HtmlTagUtils.isTagPropertyTrueAndPut(parameterMap, "readonly");

        addAttributeOfDict(model);
        return this.formPage;
    }

//    @PostMapping
//    @ResponseBody
//    public Result save(@RequestBody E e) {
//        baseService.save(e);
//        return ResultUtils.success(e);
//    }

    @PutMapping("{id}")
    @ResponseBody
    public Result update(@PathVariable Long id, @RequestBody E e) {
        e.setId(id);
        baseService.update(e);
        return ResultUtils.success(e);
    }

    @PostMapping
    @ResponseBody
    public Result saveOrUpdate(@RequestBody E e) {
        baseService.saveOrUpdate(e);
        return ResultUtils.success(e);
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public Result deleteById(@PathVariable Long id) {
        baseService.deleteLogicallyById(id);
        return ResultUtils.success();
    }

    private void addAttributeOfDict(Model model) {
        // 所有用到的字典
        EntityDAO entityDAO = EntityDAOManager.getEntityDAO(entityClass);
        Map<String, Field> fieldMap = entityDAO.getTableMeta().getFieldMap();
        Map<String, String> columnNameToPropertyNameMap = entityDAO.getColumnNameToPropertyNameMap();

        List<String> columnNames = entityDAO.getTableMeta().getSortedColumns();

        for (String columnName : columnNames) {
            if (SharpDbConstants.ID_COLUMN_NAME.equals(columnName) || SharpDbConstants.LOGIC_DELETE_COLUMN_NAME.equals(columnName)) {
                continue;
            }

            String propertyName = columnNameToPropertyNameMap.get(columnName);
            Field field = fieldMap.get(propertyName);

            // 是否是字典
            boolean isDictValue = field.getType().isEnum() || field.getType().getAnnotation(DictType.class) != null || field.getDeclaringClass() == DictValue.class;

            String dictTypeValue = null;
            if (isDictValue) {
                if (field.getType().isEnum()) {
                    dictTypeValue = field.getType().getSimpleName();
                } else {
                    Field embeddedField = fieldMap.get(StringUtils.substringBefore(propertyName, "."));
                    DictType dictType = ObjectUtils.defaultIfNull(field.getAnnotation(DictType.class), embeddedField.getAnnotation(DictType.class));
                    dictTypeValue = dictType.type();
                }

            } else if (Collection.class.isAssignableFrom(field.getType())) {
                Class<?> classGenericsType = ClassUtils.getFieldGenericClass(field);
                if (classGenericsType.isEnum()) {
                    dictTypeValue = classGenericsType.getSimpleName();
                } else if (classGenericsType == DictValue.class) {
                    dictTypeValue = field.getAnnotation(DictType.class).type();
                }
            }

            if (dictTypeValue != null) {
                model.addAttribute(dictTypeValue, DictUtils.getDict(dictTypeValue));
            }
        }

    }
}
