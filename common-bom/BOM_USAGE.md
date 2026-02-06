# common-bom 使用示例

`common-bom` 用于统一内部模块版本，不直接提供运行时功能。

## 1. 在业务项目导入 BOM

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.common</groupId>
            <artifactId>common-bom</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 2. 之后可省略版本号

```xml
<dependencies>
    <dependency>
        <groupId>com.common</groupId>
        <artifactId>common-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.common</groupId>
        <artifactId>common-starter</artifactId>
    </dependency>
</dependencies>
```

适用场景：多服务项目统一对齐 `spring-boot-common` 各模块版本。
