package com.naicha.hou.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 订单支付DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class OrderPayDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 支付方式：wechat/alipay/wallet
     */
    @NotNull(message = "支付方式不能为空")
    private String payMethod;
}

