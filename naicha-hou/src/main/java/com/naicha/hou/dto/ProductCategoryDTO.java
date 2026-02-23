package com.naicha.hou.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ProductCategoryDTO {

    /**
     * 分类ID
     */
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID
     */
    private Long parentId;

    /**
     * 层级（1-一级分类，2-二级分类）
     */
    private Integer level;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 图标
     */
    private String icon;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 状态文本
     */
    private String statusText;

    /**
     * 商品数量
     */
    private Integer productCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 子分类列表
     */
    private java.util.List<ProductCategoryDTO> children;
}

