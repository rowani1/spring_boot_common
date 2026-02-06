# common-excel 使用示例

本文档演示 `common-excel` 模块的典型用法，核心工具类为 `ExcelUtils`。

源码参考：

- `src/main/java/com/common/excel/ExcelUtils.java`
- `src/main/java/com/common/excel/ExportColumn.java`
- `src/main/java/com/common/excel/ImportResult.java`
- `src/main/java/com/common/excel/ExportRequest.java`

## 1. 注解模型导出

先定义导出模型（基于 EasyExcel 注解）：

```java
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UserExcelVO {
    @ExcelProperty("用户ID")
    private Long id;

    @ExcelProperty("用户名")
    private String username;

    @ExcelProperty("手机号")
    private String phone;
}
```

导出：

```java
List<UserExcelVO> rows = queryRows();
ExcelUtils.export(rows, UserExcelVO.class, response.getOutputStream(), "用户列表");
```

## 2. 动态列导出（前端选列）

### 2.1 数据是 `Map` 列表

```java
List<ExportColumn> columns = List.of(
        ExportColumn.of("username", "用户名"),
        ExportColumn.of("phone", "手机号"),
        ExportColumn.of("status", "状态")
);

List<Map<String, Object>> data = List.of(
        Map.of("username", "alice", "phone", "13800000001", "status", "启用"),
        Map.of("username", "bob", "phone", "13800000002", "status", "禁用")
);

ExcelUtils.exportDynamic(data, columns, response.getOutputStream(), "动态导出");
```

### 2.2 数据是对象列表

```java
List<UserDTO> data = userService.list();
List<ExportColumn> columns = List.of(
        ExportColumn.of("username", "用户名"),
        ExportColumn.of("email", "邮箱")
);

ExcelUtils.exportDynamic(
        data,
        columns,
        (item, field) -> {
            if ("username".equals(field)) {
                return item.getUsername();
            }
            if ("email".equals(field)) {
                return item.getEmail();
            }
            return null;
        },
        response.getOutputStream(),
        "用户数据"
);
```

## 3. 模板填充导出

```java
try (InputStream template = resourceLoader
        .getResource("classpath:excel/user_template.xlsx")
        .getInputStream()) {

    Map<String, Object> data = new HashMap<>();
    data.put("reportDate", "2026-02-06");
    data.put("operator", "admin");

    ExcelUtils.exportWithTemplate(template, data, response.getOutputStream());
}
```

## 4. Excel 导入

### 4.1 直接读取

```java
try (InputStream is = file.getInputStream()) {
    List<UserExcelVO> rows = ExcelUtils.read(is, UserExcelVO.class, 5000);
    userService.batchImport(rows);
}
```

### 4.2 带业务校验读取

```java
try (InputStream is = file.getInputStream()) {
    ImportResult<UserExcelVO> result = ExcelUtils.readWithValidation(
            is,
            UserExcelVO.class,
            row -> {
                if (row.getUsername() == null || row.getUsername().isBlank()) {
                    return "用户名不能为空";
                }
                if (row.getPhone() == null || row.getPhone().isBlank()) {
                    return "手机号不能为空";
                }
                return null;
            },
            5000
    );

    userService.batchImport(result.getSuccessList());
    // result.getErrorList() 可返回前端展示失败行
}
```

## 5. 注意事项

- `ExcelUtils` 会校验输出流、sheet 名称、导出列定义等参数，参数不合法会抛出 `BizException`。
- 动态导出内置了单元格公式注入防护（`= + - @` 开头会被转义）。
- 导入默认最大行数为 `10000`，可通过重载方法自定义 `maxRows`。
- Web 场景建议设置响应头：`Content-Type`、`Content-Disposition`，并处理中文文件名编码。
