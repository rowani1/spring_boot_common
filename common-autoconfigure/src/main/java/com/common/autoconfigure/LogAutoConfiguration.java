package com.common.autoconfigure;

import com.common.log.aspect.OperationLogAspect;
import com.common.log.storage.DefaultLogStorage;
import com.common.log.storage.LogStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 操作日志自动配置
 */
@AutoConfiguration
@ConditionalOnClass(OperationLogAspect.class)
@ConditionalOnProperty(prefix = "common.log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LogStorage logStorage() {
        return new DefaultLogStorage();
    }

    @Bean
    @ConditionalOnMissingBean(name = "logExecutor")
    public Executor logExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("log-async-");
        executor.setRejectedExecutionHandler((r, e) -> {
            // 拒绝策略：直接在调用者线程执行
            if (!e.isShutdown()) {
                r.run();
            }
        });
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationLogAspect operationLogAspect(LogStorage logStorage, ObjectMapper objectMapper,
                                                  Executor logExecutor) {
        return new OperationLogAspect(logStorage, objectMapper, null, logExecutor);
    }
}
