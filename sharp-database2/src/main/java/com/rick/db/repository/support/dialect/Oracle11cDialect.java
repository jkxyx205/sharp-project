package com.rick.db.repository.support.dialect;

import com.rick.db.repository.model.DatabaseType;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 9/12/19 7:24 PM
 * @Copyright: 2019 www.yodean.com. All rights reserved.
 */
public class Oracle11cDialect extends Oracle10gDialect {

    @Override
    public DatabaseType getType() {
        return DatabaseType.Oracle11c;
    }
}
