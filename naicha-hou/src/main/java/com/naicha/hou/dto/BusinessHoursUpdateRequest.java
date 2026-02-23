package com.naicha.hou.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 营业时间更新请求DTO
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@Schema(description = "营业时间更新请求")
public class BusinessHoursUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 工作日开始时间
     */
    @NotBlank(message = "工作日开始时间不能为空")
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，格式为HH:mm")
    @Schema(description = "工作日开始时间", example = "09:00")
    private String weekdayOpenTime;

    /**
     * 工作日结束时间
     */
    @NotBlank(message = "工作日结束时间不能为空")
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，格式为HH:mm")
    @Schema(description = "工作日结束时间", example = "22:00")
    private String weekdayCloseTime;

    /**
     * 周末开始时间
     */
    @NotBlank(message = "周末开始时间不能为空")
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，格式为HH:mm")
    @Schema(description = "周末开始时间", example = "10:00")
    private String weekendOpenTime;

    /**
     * 周末结束时间
     */
    @NotBlank(message = "周末结束时间不能为空")
    @Pattern(regexp = "^([0-1][0-9]|2[0-3]):[0-5][0-9]$", message = "时间格式不正确，格式为HH:mm")
    @Schema(description = "周末结束时间", example = "23:00")
    private String weekendCloseTime;

    /**
     * 是否24小时营业
     */
    @Schema(description = "是否24小时营业", example = "0")
    private Integer is24Hours;
}

