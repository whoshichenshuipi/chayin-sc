package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.MerchantRatingDTO;
import com.naicha.hou.dto.MerchantRatingQueryDTO;
import com.naicha.hou.dto.MerchantRatingStatsDTO;
import com.naicha.hou.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理员商家评分控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/merchant-rating")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员商家评分管理", description = "管理员商家评分管理相关接口")
public class AdminMerchantRatingController {

    private final MerchantService merchantService;

    /**
     * 分页查询商家评分列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询商家评分列表", description = "根据条件分页查询商家评价列表")
    public Result<IPage<MerchantRatingDTO>> getMerchantRatingList(@Valid MerchantRatingQueryDTO queryDTO) {
        try {
            log.info("分页查询商家评分列表，查询条件: {}", queryDTO);
            IPage<MerchantRatingDTO> page = merchantService.getMerchantRatingPage(queryDTO);
            return Result.success("查询成功", page);
        } catch (Exception e) {
            log.error("查询商家评分列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取商家评分统计列表
     */
    @GetMapping("/stats/list")
    @Operation(summary = "获取商家评分统计列表", description = "获取所有商家或指定商家的评分统计")
    public Result<List<MerchantRatingStatsDTO>> getMerchantRatingStatsList(
            @Parameter(description = "商家ID列表（可选，用逗号分隔）") 
            @RequestParam(required = false) List<Long> merchantIds) {
        try {
            log.info("获取商家评分统计列表，商家IDs: {}", merchantIds);
            List<MerchantRatingStatsDTO> stats = merchantService.getMerchantRatingStats(merchantIds);
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("获取商家评分统计列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据商家ID获取评分统计
     */
    @GetMapping("/stats/{merchantId}")
    @Operation(summary = "获取商家评分统计", description = "根据商家ID获取该商家的详细评分统计")
    public Result<MerchantRatingStatsDTO> getMerchantRatingStats(
            @Parameter(description = "商家ID", required = true) 
            @PathVariable @NotNull Long merchantId) {
        try {
            log.info("获取商家评分统计，商家ID: {}", merchantId);
            MerchantRatingStatsDTO stats = merchantService.getMerchantRatingStatsById(merchantId);
            return Result.success("查询成功", stats);
        } catch (Exception e) {
            log.error("获取商家评分统计失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}

