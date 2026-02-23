package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 消费偏好项DTO
 *
 * @author naicha
 * @since 2024-01-25
 */
@Data
public class ConsumptionPreferenceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 偏好名称
     */
    private String name;

    /**
     * 占比百分比
     */
    private Double percentage;
}

