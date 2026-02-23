package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 趋势图表数据DTO
 * 用于订单量趋势和销售额趋势图表
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class TrendChartDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图表分类（日期或时间标签）
     */
    private List<String> categories;

    /**
     * 图表数据（订单量或销售额）
     */
    private List<Number> data;
}

