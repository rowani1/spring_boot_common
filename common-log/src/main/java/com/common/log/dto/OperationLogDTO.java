package com.common.log.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志数据传输对象
 */
@Data
public class OperationLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 操作类型
     */
    private String action;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求参数（JSON）
     */
    private String requestParams;

    /**
     * 响应结果（JSON）
     */
    private String responseData;

    /**
     * 客户端IP
     */
    private String ip;

    /**
     * 用户代理
     */
    private String userAgent;

    /**
     * 耗时（毫秒）
     */
    private Long costTime;

    /**
     * 操作状态（0-失败，1-成功）
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
