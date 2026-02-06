# common-autoconfigure 使用示例

`common-autoconfigure` 通过 `AutoConfiguration.imports` 注册各子模块自动配置：

- `CommonAutoConfiguration`
- `FileAutoConfiguration`
- `CacheAutoConfiguration`
- `LogAutoConfiguration`
- `SecurityAutoConfiguration`

文件位置：
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

## 1. 常规使用

业务项目引入 `common-starter`（推荐）或直接引入 `common-autoconfigure` 后，满足条件的 Bean 会自动装配。

## 2. 条件装配说明

- `CacheAutoConfiguration`：存在 Redis 相关类时生效
- `FileAutoConfiguration`：根据 `common.file.storage-type` 选择 `StorageService` 实现
- `LogAutoConfiguration`：`common.log.enabled=true` 时生效
- `SecurityAutoConfiguration`：注册 `SecurityProperties` 配置绑定

## 3. 覆盖默认 Bean

默认都带 `@ConditionalOnMissingBean`，可以在业务侧自定义同类型 Bean 覆盖。

```java
@Bean
public LogStorage logStorage() {
    return log -> {
        // 自定义存储
    };
}
```
