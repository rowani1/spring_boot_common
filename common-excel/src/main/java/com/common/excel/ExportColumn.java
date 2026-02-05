package com.common.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出列定义（用于动态导出）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportColumn {

    /**
     * 字段名（对应数据Map的key或对象属性名）
     */
    private String field;

    /**
     * 列标题（Excel表头显示名称）
     */
    private String title;

    /**
     * 列宽度（可选，单位：字符数）
     */
    private Integer width;

    public ExportColumn(String field, String title) {
        this.field = field;
        this.title = title;
    }

    public static ExportColumn of(String field, String title) {
        return new ExportColumn(field, title);
    }

    public static ExportColumn of(String field, String title, Integer width) {
        return new ExportColumn(field, title, width);
    }
}
