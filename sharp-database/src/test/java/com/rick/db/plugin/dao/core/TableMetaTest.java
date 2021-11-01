package com.rick.db.plugin.dao.core;

import com.rick.db.model.ProjectDetail;

/**
 * @author Rick
 * @createdAt 2021-10-31 09:58:00
 */
public class TableMetaTest {
    public static void main(String[] args) {
        TableMeta tableMeta = TableMetaResolver.resolve(ProjectDetail.class);
        System.out.println(tableMeta);
    }
}
