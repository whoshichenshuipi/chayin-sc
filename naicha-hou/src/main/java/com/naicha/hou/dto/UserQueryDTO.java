package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class UserQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索类型：username-用户名, phone-手机号, id-账号ID
     */
    private String type;

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 账号状态：0-禁用 1-启用（正常）, 2-限制下单, 3-冻结
     * 前端传入：normal对应1, limited对应2, frozen对应3
     */
    private Integer status;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;
}

