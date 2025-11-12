package com.rick.db.config;

import com.rick.db.repository.support.dialect.AbstractDialect;
import lombok.experimental.UtilityClass;

/**
 * @author Rick.Xu
 * @date 2025/11/12 16:48
 */
@UtilityClass
public class Context {

    private static AbstractDialect dialect;

    static void setDialect(AbstractDialect dialect) {
        Context.dialect = dialect;
    }

    public static AbstractDialect getDialect() {
        return dialect;
    }
}
