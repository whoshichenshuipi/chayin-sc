package com.naicha.hou.controller;

import com.naicha.hou.service.OrderShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单配送服务测试控制器
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/test/shipment")
@RequiredArgsConstructor
@Tag(name = "配送服务测试", description = "测试订单配送服务是否正常")
public class OrderShipmentTestController {

    private final OrderShipmentService orderShipmentService;

    /**
     * 测试配送服务注入
     */
    @GetMapping("/service-test")
    @Operation(summary = "测试配送服务", description = "测试OrderShipmentService是否正常注入")
    public String testService() {
        log.info("测试OrderShipmentService注入");
        
        if (orderShipmentService != null) {
            log.info("OrderShipmentService注入成功");
            return "OrderShipmentService注入成功，服务正常运行";
        } else {
            log.error("OrderShipmentService注入失败");
            return "OrderShipmentService注入失败";
        }
    }
}
