package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * 店铺状态更新DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ShopStatusUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 营业状态 1-营业中 2-休息中 3-暂停营业
     */
    @NotNull(message = "营业状态不能为空")
    @Min(value = 1, message = "营业状态值无效")
    @Max(value = 3, message = "营业状态值无效")
    private Integer businessStatus;

    /**
     * 店铺公告
     */
    @Size(max = 500, message = "公告长度不能超过500个字符")
    private String announcement;
}
