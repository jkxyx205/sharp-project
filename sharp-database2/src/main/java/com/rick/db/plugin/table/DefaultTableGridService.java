package com.rick.db.plugin.table;

/**
 * @author Rick
 * @createdAt 2021-10-15 09:38:00
 */
public class DefaultTableGridService extends AbstractTableGridService {

    private String listSQL;

    private String summarySQL;

    private String countSQL;

    public DefaultTableGridService(String listSQL) {
        this(listSQL, null, null);
    }

    public DefaultTableGridService(String listSQL, String countSQL) {
        this(listSQL, countSQL, null);
    }

    public DefaultTableGridService(String listSQL, String countSQL, String summarySQL) {
        this.listSQL = listSQL;
        this.summarySQL = summarySQL;
        this.countSQL = countSQL;
    }

    @Override
    public String getListSQL() {
        return this.listSQL;
    }

    @Override
    public String getSummarySQL() {
        return this.summarySQL;
    }

    @Override
    public String getCountSQL() {
        return this.countSQL;
    }
}
