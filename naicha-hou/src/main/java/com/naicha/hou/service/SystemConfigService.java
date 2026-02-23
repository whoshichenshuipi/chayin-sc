package com.naicha.hou.service;

import com.naicha.hou.dto.SystemConfigDTO;

/**
 * 系统配置服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface SystemConfigService {

    /**
     * 获取基础参数配置
     *
     * @return 基础参数配置
     */
    SystemConfigDTO.BasicConfig getBasicConfig();

    /**
     * 保存基础参数配置
     *
     * @param config 基础参数配置
     * @return 是否成功
     */
    boolean saveBasicConfig(SystemConfigDTO.BasicConfig config);

    /**
     * 获取支付方式配置
     *
     * @return 支付方式配置
     */
    SystemConfigDTO.PaymentConfig getPaymentConfig();

    /**
     * 保存支付方式配置
     *
     * @param config 支付方式配置
     * @return 是否成功
     */
    boolean savePaymentConfig(SystemConfigDTO.PaymentConfig config);

    /**
     * 获取配送规则配置
     *
     * @return 配送规则配置
     */
    SystemConfigDTO.DeliveryConfig getDeliveryConfig();

    /**
     * 保存配送规则配置
     *
     * @param config 配送规则配置
     * @return 是否成功
     */
    boolean saveDeliveryConfig(SystemConfigDTO.DeliveryConfig config);

    /**
     * 测试支付连接
     *
     * @return 是否成功
     */
    boolean testPaymentConnection();
}

