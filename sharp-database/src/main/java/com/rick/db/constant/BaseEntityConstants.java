package com.rick.db.constant;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;

/**
 * @author Rick
 * @createdAt 2021-10-27 09:10:00
 */
public class BaseEntityConstants {

    public static final String LOGIC_DELETE_COLUMN_NAME = "is_deleted";

    public static final String ID_COLUMN_NAME = "id";

    public static final String CREATED_AT_COLUMN_NAME = "created_at";

    public static final String UPDATED_AT_COLUMN_NAME = "updated_at";

    public static final String CREATED_BY_COLUMN_NAME = "created_by";

    public static final String UPDATED_BY_COLUMN_NAME = "updated_by";

    public static final String COLUMN_NAME_SEPARATOR_REGEX = "\\s*,\\s*";

    public static final Converter<String, String> underscoreToCamelConverter = CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL);

}
