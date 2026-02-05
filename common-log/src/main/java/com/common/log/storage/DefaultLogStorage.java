package com.common.log.storage;

import com.common.log.dto.OperationLogDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认日志存储实现（仅打印日志）
 */
@Slf4j
public class DefaultLogStorage implements LogStorage {

    @Override
    public void save(OperationLogDTO logDTO) {
        log.info("[操作日志] traceId={}, user={}, module={}, action={}, desc={}, costTime={}ms, status={}",
                logDTO.getTraceId(),
                logDTO.getUsername(),
                logDTO.getModule(),
                logDTO.getAction(),
                logDTO.getDescription(),
                logDTO.getCostTime(),
                logDTO.getStatus() == 1 ? "成功" : "失败");
    }
}
