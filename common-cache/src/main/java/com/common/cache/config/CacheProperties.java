package com.common.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存配置属性
 */
@Data
@ConfigurationProperties(prefix = "common.cache")
public class CacheProperties {

    /**
     * 缓存Key前缀
     */
    private String keyPrefix = "app";

    /**
     * 默认过期时间（秒）
     */
    private long defaultTtl = 3600;

    /**
     * 空值缓存过期时间（秒），用于防止缓存穿透
     */
    private long nullValueTtl = 60;

    /**
     * Key分隔符
     */
    private String keySeparator = ":";
}
