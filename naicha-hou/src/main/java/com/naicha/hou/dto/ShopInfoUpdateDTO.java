package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 店铺信息更新DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ShopInfoUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    @Size(min = 2, max = 20, message = "店铺名称长度在2到20个字符")
    private String shopName;

    /**
     * 店铺Logo
     */
    private String shopLogo;

    /**
     * 店铺描述
     */
    @Size(max = 500, message = "店铺描述长度不能超过500个字符")
    private String description;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号码")
    private String contactPhone;

    /**
     * 店铺地址
     */
    @NotBlank(message = "店铺地址不能为空")
    @Size(min = 10, max = 200, message = "地址长度在10到200个字符")
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 营业时间（工作日）
     */
    private BusinessHours weekday;

    /**
     * 营业时间（周末）
     */
    private BusinessHours weekend;

    /**
     * 营业时间内部类
     */
    @Data
    public static class BusinessHours implements Serializable {
        /**
         * 开始时间
         */
        @NotBlank(message = "开始时间不能为空")
        @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确")
        private String openTime;

        /**
         * 结束时间
         */
        @NotBlank(message = "结束时间不能为空")
        @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确")
        private String closeTime;
    }
}
