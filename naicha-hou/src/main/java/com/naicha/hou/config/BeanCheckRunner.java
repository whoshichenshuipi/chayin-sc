package com.naicha.hou.config;

import com.naicha.hou.service.OrderShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * 启动时检查Bean注入
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@Component
public class BeanCheckRunner implements CommandLineRunner {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private OrderShipmentService orderShipmentService;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Bean 注入检查开始 ===");
        
        // 检查 OrderShipmentService 是否注入成功
        if (orderShipmentService != null) {
            log.info("✅ OrderShipmentService 注入成功");
        } else {
            log.error("❌ OrderShipmentService 注入失败");
        }
        
        // 检查所有相关的Bean
        String[] beanNames = {
            "orderShipmentServiceImpl",
            "orderShipmentMapper", 
            "shippingProgressMapper",
            "orderMapper"
        };
        
        for (String beanName : beanNames) {
            try {
                Object bean = applicationContext.getBean(beanName);
                log.info("✅ Bean '{}' 存在: {}", beanName, bean.getClass().getSimpleName());
            } catch (Exception e) {
                log.error("❌ Bean '{}' 不存在: {}", beanName, e.getMessage());
            }
        }
        
        log.info("=== Bean 注入检查结束 ===");
    }
}
