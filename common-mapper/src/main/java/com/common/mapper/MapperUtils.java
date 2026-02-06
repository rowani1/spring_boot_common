package com.common.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 实体转换工具类
 * <p>
 * 提供非 Spring 环境下的手动映射支持
 */
public final class MapperUtils {

    private MapperUtils() {
    }

    /**
     * 单个对象转换
     *
     * @param source    源对象
     * @param converter 转换函数
     * @param <S>       源类型
     * @param <T>       目标类型
     * @return 转换后的对象，源对象为null时返回null
     */
    public static <S, T> T convert(S source, Function<S, T> converter) {
        Objects.requireNonNull(converter, "converter must not be null");
        if (source == null) {
            return null;
        }
        return converter.apply(source);
    }

    /**
     * 列表转换
     *
     * @param sourceList 源列表
     * @param converter  转换函数
     * @param <S>        源类型
     * @param <T>        目标类型
     * @return 转换后的列表，源列表为null或空时返回空列表
     */
    public static <S, T> List<T> convertList(Collection<S> sourceList, Function<S, T> converter) {
        Objects.requireNonNull(converter, "converter must not be null");
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(converter)
                .collect(Collectors.toList());
    }

    /**
     * 列表转换（返回可修改的ArrayList）
     *
     * @param sourceList 源列表
     * @param converter  转换函数
     * @param <S>        源类型
     * @param <T>        目标类型
     * @return 转换后的ArrayList
     */
    public static <S, T> List<T> convertToArrayList(Collection<S> sourceList, Function<S, T> converter) {
        Objects.requireNonNull(converter, "converter must not be null");
        if (sourceList == null || sourceList.isEmpty()) {
            return new ArrayList<>();
        }
        return sourceList.stream()
                .map(converter)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
