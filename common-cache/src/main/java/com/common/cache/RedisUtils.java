package com.common.cache;

import com.common.cache.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Slf4j
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheProperties properties;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate, CacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    // ======================== String 操作 ========================

    /**
     * 设置缓存（使用默认过期时间）
     */
    public void set(String key, Object value) {
        set(key, value, properties.getDefaultTtl(), TimeUnit.SECONDS);
    }

    /**
     * 设置缓存（指定过期时间）
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (unit == null) {
            unit = TimeUnit.SECONDS;
        }
        if (timeout <= 0) {
            timeout = properties.getDefaultTtl();
        }
        try {
            if (value == null) {
                // 空值缓存，防止缓存穿透
                redisTemplate.opsForValue().set(key, NullValue.INSTANCE,
                        properties.getNullValueTtl(), TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            }
        } catch (Exception e) {
            log.error("Redis set 失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof NullValue) {
                return null;
            }
            return (T) value;
        } catch (Exception e) {
            log.error("Redis get 失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 获取缓存（带类型）
     */
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        return null;
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis delete 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 批量删除
     */
    public long delete(Collection<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        try {
            Long count = redisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis batch delete 失败", e);
            return 0;
        }
    }

    /**
     * 判断Key是否存在
     */
    public boolean hasKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis hasKey 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("Redis expire 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 获取过期时间（秒）
     */
    public long getExpire(String key) {
        if (key == null || key.isEmpty()) {
            return -2;
        }
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("Redis getExpire 失败: key={}", key, e);
            return -2;
        }
    }

    // ======================== 数值操作 ========================

    /**
     * 递增
     */
    public long incr(String key) {
        return incr(key, 1);
    }

    /**
     * 递增指定值
     */
    public long incr(String key, long delta) {
        if (key == null || key.isEmpty()) {
            return 0;
        }
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis incr 失败: key={}", key, e);
            return 0;
        }
    }

    /**
     * 递减
     */
    public long decr(String key) {
        return decr(key, 1);
    }

    /**
     * 递减指定值
     */
    public long decr(String key, long delta) {
        if (key == null || key.isEmpty()) {
            return 0;
        }
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis decr 失败: key={}", key, e);
            return 0;
        }
    }

    // ======================== Hash 操作 ========================

    /**
     * Hash设置
     */
    public void hSet(String key, String field, Object value) {
        if (key == null || key.isEmpty() || field == null) {
            return;
        }
        try {
            redisTemplate.opsForHash().put(key, field, value);
        } catch (Exception e) {
            log.error("Redis hSet 失败: key={}, field={}", key, field, e);
        }
    }

    /**
     * Hash获取
     */
    @SuppressWarnings("unchecked")
    public <T> T hGet(String key, String field) {
        if (key == null || key.isEmpty() || field == null) {
            return null;
        }
        try {
            return (T) redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis hGet 失败: key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * Hash删除字段
     */
    public long hDelete(String key, Object... fields) {
        if (key == null || key.isEmpty() || fields == null) {
            return 0;
        }
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("Redis hDelete 失败: key={}", key, e);
            return 0;
        }
    }

    /**
     * Hash判断字段是否存在
     */
    public boolean hHasKey(String key, String field) {
        if (key == null || key.isEmpty() || field == null) {
            return false;
        }
        try {
            return redisTemplate.opsForHash().hasKey(key, field);
        } catch (Exception e) {
            log.error("Redis hHasKey 失败: key={}, field={}", key, field, e);
            return false;
        }
    }

    /**
     * Hash获取所有
     */
    public Map<Object, Object> hGetAll(String key) {
        if (key == null || key.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis hGetAll 失败: key={}", key, e);
            return Collections.emptyMap();
        }
    }

    // ======================== Set 操作 ========================

    /**
     * Set添加
     */
    public long sAdd(String key, Object... values) {
        if (key == null || key.isEmpty() || values == null) {
            return 0;
        }
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis sAdd 失败: key={}", key, e);
            return 0;
        }
    }

    /**
     * Set获取所有成员
     */
    public Set<Object> sMembers(String key) {
        if (key == null || key.isEmpty()) {
            return Collections.emptySet();
        }
        try {
            Set<Object> members = redisTemplate.opsForSet().members(key);
            return members != null ? members : Collections.emptySet();
        } catch (Exception e) {
            log.error("Redis sMembers 失败: key={}", key, e);
            return Collections.emptySet();
        }
    }

    /**
     * Set判断是否包含
     */
    public boolean sIsMember(String key, Object value) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("Redis sIsMember 失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 空值占位符（用于缓存穿透保护）
     */
    public static class NullValue implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        public static final NullValue INSTANCE = new NullValue();
        private NullValue() {}
    }
}
