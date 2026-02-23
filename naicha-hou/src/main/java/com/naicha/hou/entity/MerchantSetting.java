package com.naicha.hou.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商家营业时间设置实体类
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("merchant_setting")
public class MerchantSetting implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商家ID
     */
    @TableField("merchant_id")
    private Long merchantId;

    /**
     * 工作日开始时间
     */
    @TableField("weekday_open_time")
    private String weekdayOpenTime;

    /**
     * 工作日结束时间
     */
    @TableField("weekday_close_time")
    private String weekdayCloseTime;

    /**
     * 周末开始时间
     */
    @TableField("weekend_open_time")
    private String weekendOpenTime;

    /**
     * 周末结束时间
     */
    @TableField("weekend_close_time")
    private String weekendCloseTime;

    /**
     * 是否24小时营业 0-否 1-是
     */
    @TableField("is_24_hours")
    private Integer is24Hours;

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

