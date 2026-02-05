package com.common.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Excel工具类
 */
@Slf4j
public final class ExcelUtils {

    private static final int DEFAULT_MAX_IMPORT_ROWS = 10000;

    private ExcelUtils() {}

    /**
     * 导出Excel（基于注解）
     */
    public static <T> void export(List<T> data, Class<T> clazz, OutputStream os) {
        export(data, clazz, os, "Sheet1");
    }

    /**
     * 导出Excel（基于注解，指定sheet名）
     */
    public static <T> void export(List<T> data, Class<T> clazz, OutputStream os, String sheetName) {
        validateOutputStream(os);
        validateSheetName(sheetName);
        if (data == null) {
            data = Collections.emptyList();
        }
        try {
            EasyExcel.write(os, clazz)
                    .registerWriteHandler(defaultStyle())
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new BizException(500, "Excel导出失败");
        }
    }

    /**
     * 动态字段导出（前端可自定义导出字段）
     *
     * @param data        数据列表
     * @param columns     导出列定义（按顺序）
     * @param os          输出流
     * @param sheetName   Sheet名称
     */
    public static void exportDynamic(List<Map<String, Object>> data, List<ExportColumn> columns,
                                     OutputStream os, String sheetName) {
        validateOutputStream(os);
        validateSheetName(sheetName);
        validateColumns(columns);
        if (data == null) {
            data = Collections.emptyList();
        }

        try {
            // 构建表头
            List<List<String>> head = new ArrayList<>();
            List<String> fieldKeys = new ArrayList<>();
            for (ExportColumn col : columns) {
                head.add(Collections.singletonList(col.getTitle()));
                fieldKeys.add(col.getField());
            }

            // 构建数据行（带公式注入防护）
            List<List<Object>> rows = new ArrayList<>();
            for (Map<String, Object> row : data) {
                if (row == null) {
                    continue;
                }
                List<Object> rowData = new ArrayList<>();
                for (String field : fieldKeys) {
                    rowData.add(sanitizeCellValue(row.get(field)));
                }
                rows.add(rowData);
            }

            EasyExcel.write(os)
                    .registerWriteHandler(defaultStyle())
                    .head(head)
                    .sheet(sheetName)
                    .doWrite(rows);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel动态导出失败", e);
            throw new BizException(500, "Excel导出失败");
        }
    }

    /**
     * 动态字段导出（从对象列表转换）
     *
     * @param data        数据列表
     * @param columns     导出列定义
     * @param extractor   字段提取器
     * @param os          输出流
     * @param sheetName   Sheet名称
     */
    public static <T> void exportDynamic(List<T> data, List<ExportColumn> columns,
                                         FieldExtractor<T> extractor,
                                         OutputStream os, String sheetName) {
        validateOutputStream(os);
        validateSheetName(sheetName);
        validateColumns(columns);
        if (extractor == null) {
            throw new BizException(400, "字段提取器不能为空");
        }
        if (data == null) {
            data = Collections.emptyList();
        }

        try {
            // 构建表头
            List<List<String>> head = new ArrayList<>();
            for (ExportColumn col : columns) {
                head.add(Collections.singletonList(col.getTitle()));
            }

            // 构建数据行（带公式注入防护）
            List<List<Object>> rows = new ArrayList<>();
            for (T item : data) {
                if (item == null) {
                    continue;
                }
                List<Object> rowData = new ArrayList<>();
                for (ExportColumn col : columns) {
                    rowData.add(sanitizeCellValue(extractor.extract(item, col.getField())));
                }
                rows.add(rowData);
            }

            EasyExcel.write(os)
                    .registerWriteHandler(defaultStyle())
                    .head(head)
                    .sheet(sheetName)
                    .doWrite(rows);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel动态导出失败", e);
            throw new BizException(500, "Excel导出失败");
        }
    }

    /**
     * 模板填充导出
     */
    public static void exportWithTemplate(InputStream templateIs, Map<String, Object> data, OutputStream os) {
        validateOutputStream(os);
        if (templateIs == null) {
            throw new BizException(400, "模板文件不能为空");
        }
        if (data == null) {
            data = Collections.emptyMap();
        }
        try {
            EasyExcel.write(os)
                    .withTemplate(templateIs)
                    .sheet()
                    .doFill(data);
        } catch (Exception e) {
            log.error("Excel模板导出失败", e);
            throw new BizException(500, "Excel模板导出失败");
        }
    }

    /**
     * 读取Excel
     */
    public static <T> List<T> read(InputStream is, Class<T> clazz) {
        return read(is, clazz, DEFAULT_MAX_IMPORT_ROWS);
    }

    /**
     * 读取Excel（带行数限制）
     */
    public static <T> List<T> read(InputStream is, Class<T> clazz, int maxRows) {
        if (is == null) {
            throw new BizException(400, "文件流不能为空");
        }
        try {
            List<T> result = EasyExcel.read(is).head(clazz).sheet().doReadSync();
            if (result != null && result.size() > maxRows) {
                throw new BizException(400, "导入数据超过最大行数限制: " + maxRows);
            }
            return result != null ? result : Collections.emptyList();
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel读取失败", e);
            throw new BizException(500, "Excel读取失败");
        }
    }

    /**
     * 读取Excel（带校验，返回成功和错误行）
     */
    public static <T> ImportResult<T> readWithValidation(InputStream is, Class<T> clazz,
                                                          RowValidator<T> validator) {
        return readWithValidation(is, clazz, validator, DEFAULT_MAX_IMPORT_ROWS);
    }

    /**
     * 读取Excel（带校验和行数限制）
     */
    public static <T> ImportResult<T> readWithValidation(InputStream is, Class<T> clazz,
                                                          RowValidator<T> validator, int maxRows) {
        if (is == null) {
            throw new BizException(400, "文件流不能为空");
        }
        if (validator == null) {
            throw new BizException(400, "校验器不能为空");
        }

        List<T> allRows = read(is, clazz, maxRows);
        List<T> successList = new ArrayList<>();
        List<ImportResult.ErrorRow> errorList = new ArrayList<>();

        int rowNum = 1;
        for (T row : allRows) {
            rowNum++;
            try {
                String error = validator.validate(row);
                if (error == null) {
                    successList.add(row);
                } else {
                    errorList.add(new ImportResult.ErrorRow(rowNum, error));
                }
            } catch (Exception e) {
                log.warn("行{}校验异常: {}", rowNum, e.getMessage());
                errorList.add(new ImportResult.ErrorRow(rowNum, "校验异常: " + e.getMessage()));
            }
        }

        return new ImportResult<>(successList, errorList);
    }

    /**
     * 清洗单元格值，防止Excel公式注入
     */
    private static Object sanitizeCellValue(Object value) {
        if (value instanceof String) {
            String str = (String) value;
            if (!str.isEmpty()) {
                char first = str.charAt(0);
                if (first == '=' || first == '+' || first == '-' || first == '@') {
                    return "'" + str;
                }
            }
        }
        return value;
    }

    private static void validateOutputStream(OutputStream os) {
        if (os == null) {
            throw new BizException(400, "输出流不能为空");
        }
    }

    private static void validateSheetName(String sheetName) {
        if (sheetName == null || sheetName.trim().isEmpty()) {
            throw new BizException(400, "Sheet名称不能为空");
        }
    }

    private static void validateColumns(List<ExportColumn> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new BizException(400, "导出列不能为空");
        }
        for (int i = 0; i < columns.size(); i++) {
            ExportColumn col = columns.get(i);
            if (col == null) {
                throw new BizException(400, "第" + (i + 1) + "列定义不能为空");
            }
            if (col.getField() == null || col.getField().trim().isEmpty()) {
                throw new BizException(400, "第" + (i + 1) + "列字段名不能为空");
            }
            if (col.getTitle() == null || col.getTitle().trim().isEmpty()) {
                throw new BizException(400, "第" + (i + 1) + "列标题不能为空");
            }
        }
    }

    private static HorizontalCellStyleStrategy defaultStyle() {
        // 表头样式
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        WriteFont headFont = new WriteFont();
        headFont.setBold(true);
        headStyle.setWriteFont(headFont);
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);

        // 内容样式
        WriteCellStyle contentStyle = new WriteCellStyle();
        contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);

        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /**
     * 字段提取器接口
     */
    @FunctionalInterface
    public interface FieldExtractor<T> {
        Object extract(T item, String field);
    }

    /**
     * 行校验器接口
     */
    @FunctionalInterface
    public interface RowValidator<T> {
        /**
         * 校验行数据
         * @return null表示通过，否则返回错误信息
         */
        String validate(T row);
    }
}
