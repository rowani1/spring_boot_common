# common-cache 使用示例

`common-cache` 主要提供 `RedisUtils` 和 `CacheKeyGenerator`。

## 1. 配置

```yaml
common:
  cache:
    key-prefix: app
    key-separator: ":"
    default-ttl: 3600
    null-value-ttl: 60
```

## 2. 生成缓存 Key

```java
@Resource
private CacheKeyGenerator cacheKeyGenerator;

String key1 = cacheKeyGenerator.generate("user", "1001");
String key2 = cacheKeyGenerator.generate("user", "1001", "tenantA");
String key3 = cacheKeyGenerator.generateWithParams("order", "detail", 1001, "v2");
```

## 3. 常用 RedisUtils

```java
@Resource
private RedisUtils redisUtils;

public UserDTO getUser(Long id) {
    String key = "user:" + id;

    UserDTO cache = redisUtils.get(key, UserDTO.class);
    if (cache != null) {
        return cache;
    }

    UserDTO user = userService.load(id);
    redisUtils.set(key, user, 10, TimeUnit.MINUTES);
    return user;
}
```

## 4. Hash / Set / 计数

```java
redisUtils.hSet("user:1001", "nickname", "alice");
String nickname = redisUtils.hGet("user:1001", "nickname");

redisUtils.sAdd("role:admin", "u1", "u2");
boolean inSet = redisUtils.sIsMember("role:admin", "u1");

long count = redisUtils.incr("api:visit:2026-02-06");
```

说明：

- `set(key, null)` 会写入空值占位，防缓存穿透。
- `RedisUtils` 内部做了异常兜底（记录日志并返回默认值）。
