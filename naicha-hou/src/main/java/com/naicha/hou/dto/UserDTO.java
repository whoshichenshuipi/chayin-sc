package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户DTO（用于管理员查看用户信息）
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 注册时间
     */
    private LocalDateTime registerTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 消费总额
     */
    private BigDecimal totalAmount;

    /**
     * 订单数量
     */
    private Integer orderCount;

    /**
     * 账号状态：0-禁用 1-正常 2-限制下单 3-冻结
     */
    private Integer status;

    /**
     * 违规记录列表
     */
    private List<UserViolationDTO> violations;
}

