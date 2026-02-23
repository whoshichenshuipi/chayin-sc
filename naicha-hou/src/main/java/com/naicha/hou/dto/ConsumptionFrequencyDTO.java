package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 消费频次分析DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class ConsumptionFrequencyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图表分类（频次段）
     */
    private List<String> categories;

    /**
     * 各频次段的客户数量
     */
    private List<Long> data;

    /**
     * 各频次段的百分比
     */
    private List<Double> percentages;

    /**
     * 统计信息列表
     */
    private List<FrequencyStatDTO> stats;
}

