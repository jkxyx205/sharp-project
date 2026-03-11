package com.rick.excel.plugin;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.db.plugin.table.AbstractTableGridService;
import com.rick.excel.table.model.MapTableColumn;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * All rights Reserved, Designed By www.xhope.top
 *
 * @version V1.0
 * @Description: 列表类型的报表，包含列表分页排序数据，列统计，列表导出
 * @author: Rick.Xu
 * @date: 12/3/20 5:33 PM
 * @Copyright: 2020 www.yodean.com. All rights reserved.
 */
public abstract class AbstractExportTableGridService extends AbstractTableGridService {

    public void export(HttpServletRequest request, HttpServletResponse response, String fileName, List<MapTableColumn> columnList) throws IOException {
        export(request, null, response, fileName, columnList);
    }

    public void export(HttpServletRequest request, Map<String, Object> extendParams, HttpServletResponse response, String fileName, List<MapTableColumn> columnList) throws IOException {
        ExportUtils.export(getListSQL(),
                HttpServletRequestUtils.getParameterMap(request, extendParams),
                HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +".xlsx"),
                columnList);
    }

}
