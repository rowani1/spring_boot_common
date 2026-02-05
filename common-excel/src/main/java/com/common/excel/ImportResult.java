package com.common.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Excel导入结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResult<T> {

    /**
     * 成功导入的数据列表
     */
    private List<T> successList = new ArrayList<>();

    /**
     * 错误行列表
     */
    private List<ErrorRow> errorList = new ArrayList<>();

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return errorList == null || errorList.isEmpty();
    }

    /**
     * 成功数量
     */
    public int getSuccessCount() {
        return successList != null ? successList.size() : 0;
    }

    /**
     * 错误数量
     */
    public int getErrorCount() {
        return errorList != null ? errorList.size() : 0;
    }

    /**
     * 错误行信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorRow {
        /**
         * 行号（从1开始，包含表头）
         */
        private int rowNum;

        /**
         * 错误信息
         */
        private String message;
    }
}
