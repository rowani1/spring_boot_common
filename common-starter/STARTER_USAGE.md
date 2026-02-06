# common-starter 使用示例

`common-starter` 是聚合入口，内部依赖 `common-autoconfigure`。

## 1. 引入依赖

```xml
<dependency>
    <groupId>com.common</groupId>
    <artifactId>common-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## 2. 配置示例

```yaml
common:
  cache:
    key-prefix: app
  file:
    storage-type: local
    local:
      upload-path: D:/data/upload
      url-prefix: /files
  security:
    desensitize-enabled: true
  log:
    enabled: true
```

## 3. 开箱可用能力

- `StorageService`（local/oss/minio）
- `RedisUtils`、`CacheKeyGenerator`
- `@OperationLog` 切面日志
- `SecurityProperties` 配置绑定
- Web 全局异常和 TraceId 过滤器（引入 `common-web` 时）
