package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 地域分布DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class RegionDistributionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 区域描述（如距离店铺距离）
     */
    private String description;

    /**
     * 客户数量
     */
    private Long count;
}

