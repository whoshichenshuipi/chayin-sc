package com.naicha.hou.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 高德地图配置
 *
 * @author naicha
 * @since 2023-12-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "amap")
public class AmapConfig {

    /**
     * Key
     */
    private String key;

    /**
     * Secret
     */
    private String secret;

    /**
     * 服务地址
     */
    private String baseUrl = "https://restapi.amap.com/v3";
}

