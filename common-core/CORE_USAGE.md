# common-core 使用示例

`common-core` 提供统一返回、错误码、业务异常和断言工具。

## 1. 统一返回 Result

```java
@GetMapping("/users/{id}")
public Result<UserDTO> getUser(@PathVariable Long id) {
    UserDTO user = userService.getById(id);
    return Result.success(user);
}

@PostMapping("/users")
public Result<Void> createUser(@RequestBody UserCreateCmd cmd) {
    userService.create(cmd);
    return Result.success("创建成功", null);
}
```

## 2. 业务异常 BizException

```java
public UserDTO getById(Long id) {
    UserDTO user = repository.find(id);
    if (user == null) {
        throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
    }
    return user;
}
```

## 3. 参数断言 Assert

```java
public void update(UserUpdateCmd cmd) {
    Assert.notNull(cmd, "请求体不能为空");
    Assert.notNull(cmd.getId(), "用户ID不能为空");
    Assert.notEmpty(cmd.getUsername(), "用户名不能为空");
    Assert.isTrue(cmd.getAge() >= 0, "年龄不能为负数");
}
```

## 4. 常见返回构造

```java
Result<Void> ok = Result.success();
Result<String> okData = Result.success("done");
Result<Void> bad = Result.fail(ResultCode.BAD_REQUEST, "参数错误");
Result<Void> err = Result.<Void>fail(500, "系统异常").traceId("abc123");
```
