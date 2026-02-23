package com.naicha.hou.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.io.Serializable;

/**
 * 评价回复DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class ReviewReplyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评价ID
     */
    @NotNull(message = "评价ID不能为空")
    private Long reviewId;

    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(max = 500, message = "回复内容长度不能超过500个字符")
    private String reply;
}
