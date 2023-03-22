package com.rick.common.http.web.param;

/**
 * @author Rick
 * @createdAt 2023-03-15 17:20:00
 */

import com.rick.common.util.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.web.servlet.mvc.method.annotation.ExtendedServletRequestDataBinder;

import javax.servlet.ServletRequest;
import java.util.Map;

/**
 * ServletRequestDataBinder which supports fields renaming using {@link ParamName}
 *
 * @author jkee
 */
public class ParamNameDataBinder extends ExtendedServletRequestDataBinder {

    private final Map<String, String> renameMapping;

    public ParamNameDataBinder(Object target, String objectName, Map<String, String> renameMapping) {
        super(target, objectName);
        this.renameMapping = renameMapping;
    }

    @Override
    protected void addBindValues(MutablePropertyValues mpvs, ServletRequest request) {
        super.addBindValues(mpvs, request);
        for (Map.Entry<String, String> entry : renameMapping.entrySet()) {
            String from = entry.getKey();
            String to = entry.getValue();
            from = StringUtils.stringToCamel(from);
            if (mpvs.contains(from)) {
                mpvs.add(to, mpvs.getPropertyValue(from).getValue());
            }
        }
    }
}
