package com.naicha.hou.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 返回结果状态码枚举
 *
 * @author naicha
 * @since 2023-12-01
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 失败
     */
    FAILED(500, "操作失败"),

    /**
     * 参数验证失败
     */
    VALIDATE_FAILED(400, "参数验证失败"),

    /**
     * 未登录
     */
    UNAUTHORIZED(401, "暂未登录或token已经过期"),

    /**
     * 未授权
     */
    FORBIDDEN(403, "没有相关权限"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 服务器内部错误
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),

    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(1000, "业务异常"),

    /**
     * 用户相关错误
     */
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_ACCOUNT_DISABLED(1004, "账号已被禁用"),
    USER_ACCOUNT_LOCKED(1005, "账号已被锁定"),

    /**
     * 订单相关错误
     */
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_ERROR(2002, "订单状态错误"),
    ORDER_CANNOT_CANCEL(2003, "订单无法取消"),
    ORDER_CANNOT_REFUND(2004, "订单无法退款"),

    /**
     * 商品相关错误
     */
    PRODUCT_NOT_FOUND(3001, "商品不存在"),
    PRODUCT_OUT_OF_STOCK(3002, "商品库存不足"),
    PRODUCT_OFFLINE(3003, "商品已下架"),

    /**
     * 支付相关错误
     */
    PAYMENT_FAILED(4001, "支付失败"),
    PAYMENT_TIMEOUT(4002, "支付超时"),
    PAYMENT_AMOUNT_ERROR(4003, "支付金额错误"),

    /**
     * 短信相关错误
     */
    SMS_SEND_FAILED(5001, "短信发送失败"),
    SMS_CODE_ERROR(5002, "验证码错误"),
    SMS_CODE_EXPIRED(5003, "验证码已过期");

    /**
     * 状态码
     */
    private final Integer code;

    /**
     * 返回消息
     */
    private final String message;
}
