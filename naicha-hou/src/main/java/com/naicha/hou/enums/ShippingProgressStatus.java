package com.naicha.hou.enums;

import lombok.Getter;

/**
 * 配送进度状态枚举
 *
 * @author naicha
 * @since 2024-01-15
 */
@Getter
public enum ShippingProgressStatus {

    /**
     * 已发货
     */
    SHIPPED("已发货", "订单已发货，配送员已接单"),

    /**
     * 配送中
     */
    ON_THE_WAY("配送中", "配送员已出发，正在配送中"),

    /**
     * 已到达小区门口
     */
    ARRIVED_COMMUNITY("已到达小区门口", "配送员已到达小区门口"),

    /**
     * 已到达楼下
     */
    ARRIVED_BUILDING("已到达楼下", "配送员已到达楼下"),

    /**
     * 正在上楼
     */
    GOING_UPSTAIRS("正在上楼", "配送员正在上楼"),

    /**
     * 已到达门口
     */
    ARRIVED_DOOR("已到达门口", "配送员已到达门口"),

    /**
     * 已送达
     */
    DELIVERED("已送达", "订单已送达，配送完成"),

    /**
     * 配送异常
     */
    EXCEPTION("配送异常", "配送过程中出现异常");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    ShippingProgressStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ShippingProgressStatus getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ShippingProgressStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态码获取描述
     */
    public static String getDescriptionByCode(String code) {
        ShippingProgressStatus status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 判断状态码是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取所有可选的进度状态（用于下拉选择）
     */
    public static ShippingProgressStatus[] getSelectableStatuses() {
        return new ShippingProgressStatus[]{
            ON_THE_WAY,
            ARRIVED_COMMUNITY,
            ARRIVED_BUILDING,
            GOING_UPSTAIRS,
            ARRIVED_DOOR,
            EXCEPTION
        };
    }
}
