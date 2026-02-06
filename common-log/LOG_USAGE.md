# common-log 使用示例

`common-log` 提供注解式操作日志：`@OperationLog` + `OperationLogAspect`。

## 1. 启用配置

```yaml
common:
  log:
    enabled: true
```

## 2. 方法上加注解

```java
@OperationLog(module = "用户", action = "新增", description = "创建用户", saveRequest = true, saveResponse = false)
@PostMapping("/users")
public Result<Long> create(@RequestBody @Valid UserCreateCmd cmd) {
    Long id = userService.create(cmd);
    return Result.success(id);
}
```

## 3. 自定义日志存储

默认是 `DefaultLogStorage`（打印日志）。如需入库，覆盖 `LogStorage` Bean：

```java
@Component
public class DbLogStorage implements LogStorage {
    @Override
    public void save(OperationLogDTO log) {
        // TODO 持久化到数据库
    }
}
```

## 4. 自定义用户信息提供者（可选）

```java
@Bean
public OperationLogAspect operationLogAspect(
        LogStorage logStorage,
        ObjectMapper objectMapper,
        @Qualifier("logExecutor") Executor logExecutor) {

    OperationLogAspect.UserInfoProvider provider = new OperationLogAspect.UserInfoProvider() {
        @Override
        public String getUserId() { return "1001"; }
        @Override
        public String getUsername() { return "admin"; }
    };

    return new OperationLogAspect(logStorage, objectMapper, provider, logExecutor);
}
```
