package com.naicha.hou.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云OSS配置类
 * 
 * @author naicha
 * @since 2024-01-01
 */
@Configuration
@ConfigurationProperties(prefix = "naicha.oss")
@Data
public class OssConfig {

    /**
     * OSS访问域名
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥Secret
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 访问域名
     */
    private String domain;

    /**
     * 路径前缀
     */
    private String pathPrefix;

    /**
     * 创建OSS客户端Bean
     * 
     * @return OSS客户端实例
     */
    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
}
