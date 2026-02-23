package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.SystemConfigDTO;
import com.naicha.hou.entity.SystemConfig;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.SystemConfigMapper;
import com.naicha.hou.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统配置服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigMapper systemConfigMapper;
    private final ObjectMapper objectMapper;

    @Override
    public SystemConfigDTO.BasicConfig getBasicConfig() {
        SystemConfig config = getConfig("basic", "basic_config");
        if (config == null || config.getConfigValue() == null) {
            return getDefaultBasicConfig();
        }
        
        try {
            return objectMapper.readValue(config.getConfigValue(), SystemConfigDTO.BasicConfig.class);
        } catch (Exception e) {
            log.error("解析基础配置失败", e);
            return getDefaultBasicConfig();
        }
    }

    @Override
    @Transactional
    public boolean saveBasicConfig(SystemConfigDTO.BasicConfig config) {
        try {
            String configValue = objectMapper.writeValueAsString(config);
            saveConfig("basic", "basic_config", configValue, "基础参数配置");
            return true;
        } catch (Exception e) {
            log.error("保存基础配置失败", e);
            throw new BusinessException(ResultCode.FAILED, "保存基础配置失败");
        }
    }

    @Override
    public SystemConfigDTO.PaymentConfig getPaymentConfig() {
        SystemConfig config = getConfig("payment", "payment_config");
        if (config == null || config.getConfigValue() == null) {
            return getDefaultPaymentConfig();
        }
        
        try {
            return objectMapper.readValue(config.getConfigValue(), SystemConfigDTO.PaymentConfig.class);
        } catch (Exception e) {
            log.error("解析支付配置失败", e);
            return getDefaultPaymentConfig();
        }
    }

    @Override
    @Transactional
    public boolean savePaymentConfig(SystemConfigDTO.PaymentConfig config) {
        try {
            String configValue = objectMapper.writeValueAsString(config);
            saveConfig("payment", "payment_config", configValue, "支付方式配置");
            return true;
        } catch (Exception e) {
            log.error("保存支付配置失败", e);
            throw new BusinessException(ResultCode.FAILED, "保存支付配置失败");
        }
    }

    @Override
    public SystemConfigDTO.DeliveryConfig getDeliveryConfig() {
        SystemConfig config = getConfig("delivery", "delivery_config");
        if (config == null || config.getConfigValue() == null) {
            return getDefaultDeliveryConfig();
        }
        
        try {
            return objectMapper.readValue(config.getConfigValue(), SystemConfigDTO.DeliveryConfig.class);
        } catch (Exception e) {
            log.error("解析配送配置失败", e);
            return getDefaultDeliveryConfig();
        }
    }

    @Override
    @Transactional
    public boolean saveDeliveryConfig(SystemConfigDTO.DeliveryConfig config) {
        try {
            String configValue = objectMapper.writeValueAsString(config);
            saveConfig("delivery", "delivery_config", configValue, "配送规则配置");
            return true;
        } catch (Exception e) {
            log.error("保存配送配置失败", e);
            throw new BusinessException(ResultCode.FAILED, "保存配送配置失败");
        }
    }

    @Override
    public boolean testPaymentConnection() {
        // TODO: 实现支付连接测试逻辑
        log.info("测试支付连接");
        return true;
    }

    /**
     * 获取配置
     */
    private SystemConfig getConfig(String configType, String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigType, configType)
               .eq(SystemConfig::getConfigKey, configKey)
               .eq(SystemConfig::getDeleted, 0)
               .last("LIMIT 1");
        return systemConfigMapper.selectOne(wrapper);
    }

    /**
     * 保存配置
     */
    private void saveConfig(String configType, String configKey, String configValue, String description) {
        SystemConfig config = getConfig(configType, configKey);
        if (config == null) {
            config = new SystemConfig();
            config.setConfigType(configType);
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setDescription(description);
            systemConfigMapper.insert(config);
        } else {
            config.setConfigValue(configValue);
            systemConfigMapper.updateById(config);
        }
    }

    /**
     * 获取默认基础配置
     */
    private SystemConfigDTO.BasicConfig getDefaultBasicConfig() {
        SystemConfigDTO.BasicConfig config = new SystemConfigDTO.BasicConfig();
        config.setSystemName("奶茶商城");
        config.setLogo("");
        config.setServicePhone("400-123-4567");
        config.setOnlineService("https://chat.example.com");
        config.setDefaultDeliveryFee(new java.math.BigDecimal("5.00"));
        config.setFreeDeliveryThreshold(new java.math.BigDecimal("30.00"));
        config.setDescription("专业的奶茶订购平台，为您提供优质的奶茶服务");
        return config;
    }

    /**
     * 获取默认支付配置
     */
    private SystemConfigDTO.PaymentConfig getDefaultPaymentConfig() {
        SystemConfigDTO.PaymentConfig config = new SystemConfigDTO.PaymentConfig();
        
        SystemConfigDTO.PaymentConfig.WechatPayConfig wechatPay = new SystemConfigDTO.PaymentConfig.WechatPayConfig();
        wechatPay.setEnabled(true);
        wechatPay.setMerchantId("");
        wechatPay.setApiKey("");
        wechatPay.setCallbackUrl("");
        
        SystemConfigDTO.PaymentConfig.AlipayConfig alipay = new SystemConfigDTO.PaymentConfig.AlipayConfig();
        alipay.setEnabled(true);
        alipay.setMerchantId("");
        alipay.setAppId("");
        alipay.setPrivateKey("");
        
        SystemConfigDTO.PaymentConfig.UnionPayConfig unionPay = new SystemConfigDTO.PaymentConfig.UnionPayConfig();
        unionPay.setEnabled(false);
        unionPay.setMerchantId("");
        unionPay.setCertPath("");
        
        config.setWechatPay(wechatPay);
        config.setAlipay(alipay);
        config.setUnionPay(unionPay);
        
        return config;
    }

    /**
     * 获取默认配送配置
     */
    private SystemConfigDTO.DeliveryConfig getDefaultDeliveryConfig() {
        SystemConfigDTO.DeliveryConfig config = new SystemConfigDTO.DeliveryConfig();
        config.setRangeType("distance");
        config.setMaxDistance(5);
        config.setSupportedRegions(java.util.Arrays.asList("downtown"));
        config.setDeliveryTimeRange(java.util.Arrays.asList("09:00", "22:00"));
        config.setBaseFee(new java.math.BigDecimal("5.00"));
        config.setFreeThreshold(new java.math.BigDecimal("30.00"));
        config.setExtraFeePerKm(new java.math.BigDecimal("2.00"));
        config.setSpecialAreas(java.util.Collections.emptyList());
        return config;
    }
}

