package com.naicha.hou.enums;

import lombok.Getter;

/**
 * 商品状态枚举
 *
 * @author naicha
 * @since 2024-01-01
 */
@Getter
public enum ProductStatus {

    /**
     * 0 - 下架
     */
    OFFLINE(0, "下架"),

    /**
     * 1 - 上架
     */
    ONLINE(1, "上架"),

    /**
     * 2 - 预售
     */
    PRESALE(2, "预售"),

    /**
     * 3 - 审核中
     */
    AUDITING(3, "审核中"),

    /**
     * 4 - 审核不通过
     */
    AUDIT_FAILED(4, "审核不通过");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 状态描述
     */
    private final String description;

    ProductStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ProductStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ProductStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据状态码获取描述
     */
    public static String getDescriptionByCode(Integer code) {
        ProductStatus status = getByCode(code);
        return status != null ? status.getDescription() : "未知";
    }

    /**
     * 判断状态码是否有效
     */
    public static boolean isValid(Integer code) {
        return getByCode(code) != null;
    }
}

