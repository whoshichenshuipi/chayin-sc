package com.naicha.hou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 店铺信息更新请求DTO
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@Schema(description = "店铺信息更新请求")
public class ShopInfoUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 店铺名称
     */
    @NotBlank(message = "店铺名称不能为空")
    @Size(min = 2, max = 100, message = "店铺名称长度必须在2-100个字符之间")
    @Schema(description = "店铺名称", example = "甜蜜时光奶茶店")
    private String shopName;

    /**
     * 店铺Logo
     */
    @Schema(description = "店铺Logo", example = "https://example.com/logo.jpg")
    private String shopLogo;

    /**
     * 联系电话
     */
    @NotBlank(message = "联系电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    @Schema(description = "联系电话", example = "13900139000")
    private String contactPhone;

    /**
     * 店铺地址
     */
    @NotBlank(message = "店铺地址不能为空")
    @Size(min = 10, max = 255, message = "店铺地址长度必须在10-255个字符之间")
    @Schema(description = "店铺地址", example = "北京市朝阳区三里屯街道123号")
    private String address;

    /**
     * 经度
     */
    @Schema(description = "经度", example = "116.397428")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @Schema(description = "纬度", example = "39.90923")
    private BigDecimal latitude;

    /**
     * 店铺简介
     */
    @Size(max = 500, message = "店铺简介长度不能超过500个字符")
    @Schema(description = "店铺简介", example = "专注手工奶茶10年，为您提供最纯正的口感体验")
    private String description;
}

