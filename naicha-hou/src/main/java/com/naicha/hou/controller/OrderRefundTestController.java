package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.OrderRefundStatsDTO;
import com.naicha.hou.service.OrderRefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单退款测试控制器
 *
 * @author naicha
 * @since 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/api/test/refund")
@RequiredArgsConstructor
public class OrderRefundTestController {

    private final OrderRefundService orderRefundService;

    /**
     * 测试退款统计数据接口
     */
    @GetMapping("/stats")
    public Result<OrderRefundStatsDTO> testStats() {
        log.info("测试退款统计数据接口");
        
        OrderRefundStatsDTO stats = orderRefundService.getRefundStats(1L);
        return Result.success("测试成功", stats);
    }
}
