package com.rick.excel.plugin;

import com.rick.common.http.HttpServletRequestUtils;
import com.rick.common.http.HttpServletResponseUtils;
import com.rick.db.plugin.page.QueryModel;
import com.rick.excel.table.model.MapTableColumn;
import lombok.experimental.UtilityClass;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author Rick
 * @createdAt 2021-09-05 12:00:00
 */
@UtilityClass
public final class ExportHttpServletRequestUtils {

    /**
     * 报表导出
     * @param sql
     * @param request
     * @param extendParams 额外的参数信息
     * @param response
     * @param fileName 导出的文件名，不带扩展名
     * @param columnList 导出列的内容和样式信息
     * @throws IOException
     */
    public static void export(String sql, HttpServletRequest request, Map<String, ?> extendParams, HttpServletResponse response, String fileName, List<MapTableColumn> columnList) throws IOException {
        Map<String, Object> params = HttpServletRequestUtils.getParameterMap(request, extendParams);
        QueryModel queryModel = QueryModel.of(params);
        queryModel.getPageModel().setSize(-1);
        ExportUtils.export(sql,
                params,
                HttpServletResponseUtils.getOutputStreamAsAttachment(request, response, fileName + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +".xlsx"),
                columnList
                );
    }

    public static final void export(String sql, HttpServletRequest request, HttpServletResponse response, String fileName, List<MapTableColumn> columnList) throws IOException {
        export(sql, request, null, response, fileName, columnList);
    }
}
