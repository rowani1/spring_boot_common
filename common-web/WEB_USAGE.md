# common-web 使用示例

`common-web` 提供两项能力：

- 全局异常处理 `GlobalExceptionHandler`
- 请求链路追踪 `TraceIdFilter`

## 1. 依赖后默认效果

- 响应头会自动带上 `X-Trace-Id`
- `BizException`、参数校验异常、系统异常会统一转换为 `Result`

## 2. Controller 示例

```java
@GetMapping("/demo")
public Result<String> demo(@RequestParam String name) {
    if (name.length() < 2) {
        throw new BizException(ResultCode.BAD_REQUEST, "name 太短");
    }
    return Result.success("ok:" + name);
}
```

## 3. 读取 traceId

```java
String traceId = MDC.get("traceId");
log.info("traceId={}", traceId);
```

## 4. 客户端透传

客户端可传入 `X-Trace-Id`，满足格式校验时会透传；否则框架自动生成。
