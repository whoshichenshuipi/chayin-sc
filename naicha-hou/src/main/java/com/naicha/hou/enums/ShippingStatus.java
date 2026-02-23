package com.naicha.hou.enums;

import lombok.Getter;

/**
 * 配送状态枚举
 *
 * @author naicha
 * @since 2024-01-15
 */
@Getter
public enum ShippingStatus {

    /**
     * pending - 待发货
     */
    PENDING("pending", "待发货"),

    /**
     * shipping - 配送中
     */
    SHIPPING("shipping", "配送中"),

    /**
     * delivered - 已送达
     */
    DELIVERED("delivered", "已送达"),

    /**
     * exception - 配送异常
     */
    EXCEPTION("exception", "配送异常");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;

    ShippingStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ShippingStatus getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ShippingStatus status : values()) {
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
        ShippingStatus status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 判断状态码是否有效
     */
    public static boolean isValid(String code) {
        return getByCode(code) != null;
    }

    /**
     * 判断是否可以发货
     */
    public static boolean canShip(String status) {
        return PENDING.getCode().equals(status);
    }

    /**
     * 判断是否可以更新进度
     */
    public static boolean canUpdateProgress(String status) {
        return SHIPPING.getCode().equals(status);
    }

    /**
     * 判断是否可以完成配送
     */
    public static boolean canComplete(String status) {
        return SHIPPING.getCode().equals(status);
    }
}
