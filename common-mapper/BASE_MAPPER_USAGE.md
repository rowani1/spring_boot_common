# BaseMapper 使用示例

本文档演示 `common-mapper` 模块中 `BaseMapper<E, D>` 的典型用法。

## 1. BaseMapper 提供的方法

`BaseMapper` 定义了四个通用转换方法：

- `D toDto(E entity)`
- `E toEntity(D dto)`
- `List<D> toDtoList(List<E> entityList)`
- `List<E> toEntityList(List<D> dtoList)`

源码位置：`src/main/java/com/common/mapper/BaseMapper.java`

## 2. 定义业务 Mapper

基于 MapStruct，新建一个业务 Mapper 并继承 `BaseMapper`：

```java
package com.example.user.mapper;

import com.common.mapper.BaseMapper;
import com.common.mapper.config.MapStructConfig;
import com.example.user.dto.UserDTO;
import com.example.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface UserMapper extends BaseMapper<User, UserDTO> {
}
```

说明：

- `MapStructConfig` 已配置 `componentModel = "spring"`，生成类会注册为 Spring Bean。
- 一般无需手写实现类，编译期由 MapStruct 自动生成。

## 3. 在 Service 中使用

```java
package com.example.user.service;

import com.example.user.dto.UserDTO;
import com.example.user.entity.User;
import com.example.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public UserDTO toDto(User user) {
        return userMapper.toDto(user);
    }

    public User toEntity(UserDTO dto) {
        return userMapper.toEntity(dto);
    }

    public List<UserDTO> toDtoList(List<User> users) {
        return userMapper.toDtoList(users);
    }
}
```

## 4. 非 Spring 场景（可选）

如果不在 Spring 容器中，可以使用 `MapperUtils` 做函数式转换：

```java
UserDTO dto = MapperUtils.convert(user, mapper::toDto);
List<UserDTO> dtoList = MapperUtils.convertList(userList, mapper::toDto);
```

源码位置：`src/main/java/com/common/mapper/MapperUtils.java`

## 5. 常见注意事项

- 需要启用注解处理器，确保 `mapstruct-processor` 生效。
- 字段名不一致时，使用 `@Mapping` 显式声明映射关系。
- 复杂对象转换可在业务 Mapper 中补充自定义方法。
