package com.common.log.storage;

import com.common.log.dto.OperationLogDTO;

/**
 * 日志存储接口
 */
public interface LogStorage {

    /**
     * 保存操作日志
     *
     * @param log 日志对象
     */
    void save(OperationLogDTO log);
}
