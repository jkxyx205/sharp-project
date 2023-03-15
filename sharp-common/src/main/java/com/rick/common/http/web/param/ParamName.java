package com.rick.common.http.web.param;

import java.lang.annotation.*;

/**
 * Overrides parameter name
 * @author jkee
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamName {

    /**
     * The name of the request parameter to bind to.
     */
    String value();

}
