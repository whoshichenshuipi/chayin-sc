package com.naicha.hou.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     */
    private Integer code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功");
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(200, message);
    }

    /**
     * 成功返回结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> failed() {
        return new Result<>(500, "操作失败");
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> failed(String message) {
        return new Result<>(500, message);
    }

    /**
     * 失败返回结果
     */
    public static <T> Result<T> failed(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> Result<T> validateFailed() {
        return failed(400, "参数验证失败");
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> Result<T> validateFailed(String message) {
        return new Result<>(400, message);
    }

    /**
     * 未登录返回结果
     */
    public static <T> Result<T> unauthorized() {
        return failed(401, "暂未登录或token已经过期");
    }

    /**
     * 未授权返回结果
     */
    public static <T> Result<T> forbidden() {
        return failed(403, "没有相关权限");
    }

    /**
     * 错误返回结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 错误返回结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message);
    }
}
