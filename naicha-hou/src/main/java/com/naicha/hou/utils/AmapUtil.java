package com.naicha.hou.utils;

import com.naicha.hou.config.AmapConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 高德地图工具类
 *
 * @author naicha
 * @since 2023-12-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AmapUtil {

    private final AmapConfig amapConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 地理编码（地址转坐标）
     *
     * @param address 地址
     * @return 坐标 [经度, 纬度]
     */
    public BigDecimal[] geocode(String address) {
        try {
            String url = amapConfig.getBaseUrl() + "/geocode/geo";

            Map<String, String> params = new HashMap<>();
            params.put("key", amapConfig.getKey());
            params.put("address", address);

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);

            String fullUrl = url + "?" + sb.toString();
            log.info("高德地图API请求: {}", fullUrl.replace(amapConfig.getKey(), "***"));

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(fullUrl, Map.class);

            if (response != null && "1".equals(response.get("status"))) {
                Object geocodesObj = response.get("geocodes");
                if (geocodesObj instanceof java.util.List && !((java.util.List<?>) geocodesObj).isEmpty()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> geocode = (Map<String, Object>) ((java.util.List<?>) geocodesObj).get(0);
                    String location = (String) geocode.get("location");
                    String[] coordinates = location.split(",");
                    return new BigDecimal[]{
                            new BigDecimal(coordinates[0]),
                            new BigDecimal(coordinates[1])
                    };
                }
            }

            log.warn("地理编码失败: {}", response);
            return null;
        } catch (Exception e) {
            log.error("调用高德地图API失败", e);
            return null;
        }
    }

    /**
     * 逆地理编码（坐标转地址）
     *
     * @param longitude 经度
     * @param latitude 纬度
     * @return 地址
     */
    public String reverseGeocode(BigDecimal longitude, BigDecimal latitude) {
        try {
            String url = amapConfig.getBaseUrl() + "/geocode/regeo";

            Map<String, String> params = new HashMap<>();
            params.put("key", amapConfig.getKey());
            params.put("location", longitude + "," + latitude);

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);

            String fullUrl = url + "?" + sb.toString();
            log.info("高德地图API请求: {}", fullUrl.replace(amapConfig.getKey(), "***"));

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(fullUrl, Map.class);

            if (response != null && "1".equals(response.get("status"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> regeocode = (Map<String, Object>) response.get("regeocode");
                String formattedAddress = (String) regeocode.get("formatted_address");
                return formattedAddress;
            }

            log.warn("逆地理编码失败: {}", response);
            return null;
        } catch (Exception e) {
            log.error("调用高德地图API失败", e);
            return null;
        }
    }
}

