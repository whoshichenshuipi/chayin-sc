package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品规格实体类
 * 用于存储甜度、温度、加料、杯型等规格选项
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_spec")
public class ProductSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 规格ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    @TableField("product_id")
    private Long productId;

    /**
     * 规格类型：sweetness-甜度, temperature-温度, addon-加料, size-杯型
     */
    @TableField("spec_type")
    private String specType;

    /**
     * 规格名称（如：全糖、七分糖、珍珠、椰果、中杯、大杯等）
     */
    @TableField("spec_name")
    private String specName;

    /**
     * 规格值（JSON格式，存储更复杂的数据）
     * 如加料：{"name":"珍珠","price":2.00,"stock":100}
     * 杯型：{"name":"大杯","price":3.00,"capacity":"700ml"}
     */
    @TableField("spec_value")
    private String specValue;

    /**
     * 额外价格（加料、杯型等）
     */
    @TableField("extra_price")
    private BigDecimal extraPrice;

    /**
     * 库存（针对加料、杯型等）
     */
    @TableField("stock")
    private Integer stock;

    /**
     * 排序
     */
    @TableField("sort")
    private Integer sort;

    /**
     * 状态 0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 删除标志 0-未删除 1-已删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}

