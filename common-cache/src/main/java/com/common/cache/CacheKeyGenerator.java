package com.common.cache;

import com.common.cache.config.CacheProperties;

/**
 * 缓存Key生成器
 */
public class CacheKeyGenerator {

    private final CacheProperties properties;

    public CacheKeyGenerator(CacheProperties properties) {
        this.properties = properties;
    }

    /**
     * 生成缓存Key
     *
     * @param module  模块名
     * @param bizKey  业务键
     * @return 完整的缓存Key
     */
    public String generate(String module, String bizKey) {
        return generate(module, bizKey, null);
    }

    /**
     * 生成缓存Key（带租户）
     *
     * @param module   模块名
     * @param bizKey   业务键
     * @param tenantId 租户ID（可选）
     * @return 完整的缓存Key
     */
    public String generate(String module, String bizKey, String tenantId) {
        StringBuilder sb = new StringBuilder();
        String sep = properties.getKeySeparator();
        if (sep == null || sep.isEmpty()) {
            sep = ":";
        }

        // 前缀
        String prefix = properties.getKeyPrefix();
        if (prefix != null && !prefix.isEmpty()) {
            sb.append(prefix).append(sep);
        }

        // 租户
        if (tenantId != null && !tenantId.isEmpty()) {
            sb.append(tenantId).append(sep);
        }

        // 模块
        if (module != null && !module.isEmpty()) {
            sb.append(module).append(sep);
        }

        // 业务键
        if (bizKey != null && !bizKey.isEmpty()) {
            sb.append(bizKey);
        }

        // 移除末尾分隔符
        String key = sb.toString();
        if (key.endsWith(sep)) {
            key = key.substring(0, key.length() - sep.length());
        }

        return key;
    }

    /**
     * 生成带参数的缓存Key
     *
     * @param module 模块名
     * @param bizKey 业务键
     * @param params 参数列表
     * @return 完整的缓存Key
     */
    public String generateWithParams(String module, String bizKey, Object... params) {
        String baseKey = generate(module, bizKey);
        if (params == null || params.length == 0) {
            return baseKey;
        }

        StringBuilder sb = new StringBuilder(baseKey);
        String sep = properties.getKeySeparator();
        if (sep == null || sep.isEmpty()) {
            sep = ":";
        }
        for (Object param : params) {
            sb.append(sep).append(param != null ? param.toString() : "null");
        }
        return sb.toString();
    }
}
