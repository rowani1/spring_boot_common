package com.common.security.desensitize;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.lang.annotation.*;

/**
 * 数据脱敏注解
 * 使用在字段上，配合Jackson序列化进行脱敏
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@JsonSerialize(using = DesensitizeSerializer.class)
public @interface Desensitize {

    /**
     * 脱敏类型
     */
    DesensitizeType type() default DesensitizeType.CUSTOM;

    /**
     * 前缀保留长度（自定义类型使用）
     */
    int prefixLen() default 0;

    /**
     * 后缀保留长度（自定义类型使用）
     */
    int suffixLen() default 0;

    /**
     * 替换字符
     */
    char maskChar() default '*';
}
