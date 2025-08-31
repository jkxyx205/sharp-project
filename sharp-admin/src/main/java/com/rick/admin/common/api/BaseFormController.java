package com.rick.admin.common.api;

import com.rick.admin.common.exception.ResourceNotFoundException;
import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.model.Result;
import com.rick.common.http.model.ResultUtils;
import com.rick.common.util.ClassUtils;
import com.rick.common.util.HtmlTagUtils;
import com.rick.db.plugin.BaseServiceImpl;
import com.rick.db.repository.EntityDAO;
import com.rick.db.repository.EntityDAOManager;
import com.rick.db.repository.model.EntityId;
import com.rick.db.repository.support.TableMeta;
import com.rick.meta.dict.model.DictType;
import com.rick.meta.dict.model.DictValue;
import com.rick.meta.dict.service.DictUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.lang.reflect.Field;
import java.util.*;

import static com.rick.db.repository.support.Constants.ID_COLUMN_NAME;
import static com.rick.db.repository.support.Constants.LOGIC_DELETE_COLUMN_NAME;

/**
 * 表单的增删改
 * @author Rick.Xu
 * @date 2024/1/26 16:05
 */
public class BaseFormController<S extends BaseServiceImpl<? extends EntityDAO<T, ID>, T, ID>, T extends EntityId<ID>, ID> {

    protected final S baseService;

    private final String formPage;

    private final String entityPropertyName;

    private final Class<T> entityClass;

    public BaseFormController(S service, String formPage) {
        this.baseService = service;
        this.formPage = formPage;
        this.entityClass = (Class<T>) ClassUtils.getClassGenericsTypes(this.getClass())[1];
//        baseService.getBaseDAO().getEntityClass();

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
    public String gotoFormPageById(HttpServletRequest request, @PathVariable ID id, Model model) {
        Map<String, String> parameterMap = HttpServletRequestUtils.getParameterStringMap(request);

        Optional<T> op = baseService.selectById(id);
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
//    public Result save(@RequestBody T e) {
//        baseService.save(e);
//        return ResultUtils.success(e);
//    }

    @PutMapping("{id}")
    @ResponseBody
    public Result update(@PathVariable ID id, @RequestBody T e) {
        e.setId(id);
        baseService.update(e);
        return ResultUtils.success(e.getId());
    }

    @PostMapping
    @ResponseBody
    public Result saveOrUpdate(@RequestBody @Valid T e) {
        baseService.insertOrUpdate(e);
        return ResultUtils.success(e.getId());
    }

    @DeleteMapping("{id}")
    @ResponseBody
    public Result deleteById(@PathVariable ID id) {
        baseService.deleteById(id);
        return ResultUtils.success();
    }

    private void addAttributeOfDict(Model model) {
        // 所有用到的字典
        EntityDAO entityDAO = EntityDAOManager.getDAO(entityClass);
        TableMeta tableMeta = entityDAO.getTableMeta();

        List<String> columnNames = entityDAO.getTableMeta().getSortedColumns();

        for (String columnName : columnNames) {
            if (ID_COLUMN_NAME.equals(columnName) || LOGIC_DELETE_COLUMN_NAME.equals(columnName)) {
                continue;
            }

            Map<String, String> columnPropertyNameMap = tableMeta.getColumnPropertyNameMap();
            String propertyName = columnPropertyNameMap.get(columnName);

            Field field = tableMeta.getFieldByColumnName(columnName);

            // 是否是字典
            boolean isDictValue = field.getType().isEnum() || field.getType().getAnnotation(DictType.class) != null || field.getDeclaringClass() == DictValue.class;

            String dictTypeValue = null;
            if (isDictValue) {
                if (field.getType().isEnum()) {
                    dictTypeValue = field.getType().getSimpleName();
                } else {
//                    entityClass.getName()
                    Field embeddedField = ClassUtils.getField(entityClass, StringUtils.substringBefore(propertyName, "."));
//                    Field embeddedField = tableMeta.getFieldByPropertyName(StringUtils.substringBefore(propertyName, "."));
                    DictType dictType = ObjectUtils.defaultIfNull(field.getAnnotation(DictType.class), embeddedField.getAnnotation(DictType.class));
                    dictTypeValue = dictType.type();
                }

            } else if (Collection.class.isAssignableFrom(field.getType())) {
                Class<?> classGenericsType = ClassUtils.getFieldGenericClass(field)[0];
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
