package com.naicha.hou.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 系统配置DTO
 *
 * @author naicha
 * @since 2024-01-01
 */
@Data
public class SystemConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 基础参数配置
     */
    @Data
    public static class BasicConfig implements Serializable {
        private String systemName;
        private String logo;
        private String servicePhone;
        private String onlineService;
        private BigDecimal defaultDeliveryFee;
        private BigDecimal freeDeliveryThreshold;
        private String description;
    }

    /**
     * 支付方式配置
     */
    @Data
    public static class PaymentConfig implements Serializable {
        private WechatPayConfig wechatPay;
        private AlipayConfig alipay;
        private UnionPayConfig unionPay;

        @Data
        public static class WechatPayConfig implements Serializable {
            private Boolean enabled;
            private String merchantId;
            private String apiKey;
            private String callbackUrl;
        }

        @Data
        public static class AlipayConfig implements Serializable {
            private Boolean enabled;
            private String merchantId;
            private String appId;
            private String privateKey;
        }

        @Data
        public static class UnionPayConfig implements Serializable {
            private Boolean enabled;
            private String merchantId;
            private String certPath;
        }
    }

    /**
     * 配送规则配置
     */
    @Data
    public static class DeliveryConfig implements Serializable {
        private String rangeType; // distance | region
        private Integer maxDistance;
        private List<String> supportedRegions;
        private List<String> deliveryTimeRange;
        private BigDecimal baseFee;
        private BigDecimal freeThreshold;
        private BigDecimal extraFeePerKm;
        private List<SpecialArea> specialAreas;

        @Data
        public static class SpecialArea implements Serializable {
            private String name;
            private String reason;
        }
    }
}

