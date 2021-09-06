package com.rick.db.dto;

import com.google.common.collect.Maps;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * @author Rick.Xu
 * @date 2016/03/22
 */
@Data
public class PageModel {

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_SIDX = "sidx";
    public static final String PARAM_SORD = "sord";

    private Integer page = 1; // 当前页

    private Integer size = 15; // 每页显示条数,size == -1 一次性全部加载出来,不再分页

    private String sidx; // 排序字段

    private String sord; // 排序方式

    public PageModel() {}

    public PageModel(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageModel(int page, int size, String sidx, String sord) {
        this.page = page;
        this.size = size;
        this.sidx = sidx;
        this.sord = sord;
    }

    public Map<String, Object> toMap() {
        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(this.getClass());

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(propertyDescriptors.length);

        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            String name = propertyDescriptor.getName();
            if ("class".equals(name)) {
                continue;
            }

            try {
                params.put(name, propertyDescriptor.getReadMethod().invoke(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return params;
    }

    public boolean isPageQueryModel() {
        return getSize() != -1;
    }

    public boolean isAllQueryModel() {
        return getSize() == -1;
    }

}
