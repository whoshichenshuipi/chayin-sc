package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 创建通知DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class NotificationCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long recipientId;

    /**
     * 接收者类型：admin-管理员，merchant-商家
     */
    @NotBlank(message = "接收者类型不能为空")
    private String recipientType;

    /**
     * 通知类型
     */
    @NotBlank(message = "通知类型不能为空")
    private String type;

    /**
     * 通知标题
     */
    @NotBlank(message = "通知标题不能为空")
    private String title;

    /**
     * 通知内容
     */
    @NotBlank(message = "通知内容不能为空")
    private String content;

    /**
     * 优先级：1-低，2-普通，3-重要，4-紧急
     */
    private Integer priority = 2;

    /**
     * 关联数据ID
     */
    private Long relatedId;

    /**
     * 关联数据类型
     */
    private String relatedType;

    /**
     * 操作按钮列表
     */
    private List<NotificationAction> actions;

    /**
     * 扩展数据
     */
    private Map<String, Object> extraData;

    /**
     * 通知操作按钮
     */
    @Data
    public static class NotificationAction implements Serializable {
        private Long id;
        private String text;
        private String type; // primary, success, warning, danger
        private String action; // 操作类型
    }
}

