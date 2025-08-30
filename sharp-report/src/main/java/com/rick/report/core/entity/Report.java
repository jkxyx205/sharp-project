package com.rick.report.core.entity;

import com.rick.db.repository.Column;
import com.rick.db.repository.Table;
import com.rick.db.repository.model.BaseCodeEntity;
import com.rick.report.core.model.QueryField;
import com.rick.report.core.model.ReportColumn;
import com.rick.report.core.model.SordEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: (用一句话描述该文件做什么)
 * @author: Rick.Xu
 * @date: 6/18/20 3:21 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Table(value = "sys_report", comment = "报表")
public class Report extends BaseCodeEntity<Long> {

    /**
     * 是否分页
     */
    private Boolean pageable = true;

    private String sidx;

    private SordEnum sord;

    /**
     * 报表名称
     */
    @NotNull
    private String name;

//    @NotNull
//    @Column(columnDefinition = "text not null")
    private String querySql;

    private String summaryColumnNames;

    @NotNull
    @Column(value = "report_column_list", columnDefinition = "text")
    private List<ReportColumn> reportColumnList;

    @Column(columnDefinition = "text")
    private List<QueryField> queryFieldList;

    @Column(comment = "模版名称")
    private String tplName;

    @Column(comment = "其他信息")
    private Map<String, Object> additionalInfo;

    private String reportAdviceName;

    public String getUrl() {
        return "/report/" + this.getId() + "/?" + getParams();
    }

    public long getVisibleColumnSize() {
        return this.getReportColumnList().stream().filter(reportColumn -> !reportColumn.getHidden()).count();
    }

    /**
     * 初始化url地址
     * @return
     */
    private String getParams() {
        StringBuilder sb = new StringBuilder();
        if (pageable) {
            sb.append("&page=1&size=15");
        }

        if (StringUtils.isBlank(sidx)) {
            return sb.toString();
        }

        sb.append("&sidx=").append(sidx);

        if (Objects.nonNull(sord)) {
            sb.append("&sord=").append(sord.name().toLowerCase());
        } else {
            sb.append("&sord=").append("desc");
        }

        sb.deleteCharAt(0);
        return sb.toString();
    }

}
