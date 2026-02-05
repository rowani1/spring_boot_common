package com.common.excel;

import lombok.Data;

import java.util.List;

/**
 * 动态导出请求（前端传入）
 */
@Data
public class ExportRequest {

    /**
     * 导出的列定义（前端选择的字段）
     */
    private List<ExportColumn> columns;

    /**
     * 文件名（不含后缀）
     */
    private String fileName;

    /**
     * Sheet名称
     */
    private String sheetName = "Sheet1";
}
