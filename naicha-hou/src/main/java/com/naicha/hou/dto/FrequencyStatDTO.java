package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消费频次统计项DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class FrequencyStatDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签
     */
    private String label;

    /**
     * 数值（人数和百分比）
     */
    private String value;

    /**
     * 趋势（百分比变化，正数表示增长，负数表示下降）
     */
    private Double trend;
}

