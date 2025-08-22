package com.rick.db.repository.support.dialect;

import com.rick.db.repository.model.DatabaseType;

/**
 * @author Rick.Xu
 * @date 2025/8/22 10:09
 */
public class MySQL8Dialect extends MySQL5Dialect {

    @Override
    public DatabaseType getType() {
        return DatabaseType.MySQL8;
    }
}
