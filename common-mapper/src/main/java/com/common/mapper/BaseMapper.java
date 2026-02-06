package com.common.mapper;

import java.util.List;

/**
 * 基础 Mapper 接口
 * <p>
 * 提供实体与DTO之间的通用转换方法，子接口继承后由 MapStruct 生成实现类
 *
 * @param <E> Entity 实体类型
 * @param <D> DTO 数据传输对象类型
 */
public interface BaseMapper<E, D> {

    /**
     * Entity 转 DTO
     *
     * @param entity 实体对象
     * @return DTO对象
     */
    D toDto(E entity);

    /**
     * DTO 转 Entity
     *
     * @param dto DTO对象
     * @return 实体对象
     */
    E toEntity(D dto);

    /**
     * Entity 列表转 DTO 列表
     *
     * @param entityList 实体列表
     * @return DTO列表
     */
    List<D> toDtoList(List<E> entityList);

    /**
     * DTO 列表转 Entity 列表
     *
     * @param dtoList DTO列表
     * @return 实体列表
     */
    List<E> toEntityList(List<D> dtoList);
}
