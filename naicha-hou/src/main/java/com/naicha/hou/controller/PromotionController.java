package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.PromotionCreateDTO;
import com.naicha.hou.dto.PromotionDTO;
import com.naicha.hou.dto.PromotionQueryDTO;
import com.naicha.hou.dto.PromotionUserCartCreateDTO;
import com.naicha.hou.service.PromotionService;
import com.naicha.hou.service.PromotionUserCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 促销活动管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/promotion")
@RequiredArgsConstructor
@Validated
@Tag(name = "促销活动管理", description = "促销活动增删改查、暂停恢复等接口")
public class PromotionController {

    private final PromotionService promotionService;
    private final PromotionUserCartService promotionUserCartService;

    /**
     * 创建促销活动
     */
    @PostMapping("/create")
    @Operation(summary = "创建促销活动", description = "商家创建新促销活动")
    public Result<Long> createPromotion(
            @Parameter(description = "商家ID", required = true)
            @RequestHeader(value = "X-Merchant-Id", required = false, defaultValue = "1") Long merchantId,
            @Valid @RequestBody PromotionCreateDTO createDTO) {
        log.info("创建促销活动请求，商家ID: {}", merchantId);

        Long promotionId = promotionService.createPromotion(merchantId, createDTO);
        return Result.success("促销活动创建成功", promotionId);
    }

    /**
     * 更新促销活动
     */
    @PutMapping("/update/{promotionId}")
    @Operation(summary = "更新促销活动", description = "更新促销活动信息")
    public Result<Boolean> updatePromotion(
            @Parameter(description = "促销活动ID", required = true)
            @PathVariable @NotNull Long promotionId,
            @Valid @RequestBody PromotionCreateDTO createDTO) {
        log.info("更新促销活动请求，活动ID: {}", promotionId);

        boolean success = promotionService.updatePromotion(promotionId, createDTO);
        return Result.success("促销活动更新成功", success);
    }

    /**
     * 删除促销活动
     */
    @DeleteMapping("/delete/{promotionId}")
    @Operation(summary = "删除促销活动", description = "删除促销活动（只能删除未开始或已结束的活动）")
    public Result<Boolean> deletePromotion(
            @Parameter(description = "促销活动ID", required = true)
            @PathVariable @NotNull Long promotionId) {
        log.info("删除促销活动请求，活动ID: {}", promotionId);

        boolean success = promotionService.deletePromotion(promotionId);
        return Result.success("促销活动删除成功", success);
    }

    /**
     * 查询促销活动详情
     */
    @GetMapping("/detail/{promotionId}")
    @Operation(summary = "查询促销活动详情", description = "根据活动ID查询促销活动详细信息")
    public Result<PromotionDTO> getPromotionDetail(
            @Parameter(description = "促销活动ID", required = true)
            @PathVariable @NotNull Long promotionId) {
        log.info("查询促销活动详情，活动ID: {}", promotionId);

        PromotionDTO promotionDTO = promotionService.getPromotionById(promotionId);
        return Result.success("查询成功", promotionDTO);
    }

    /**
     * 暂停/恢复促销活动
     */
    @PutMapping("/toggle-pause/{promotionId}")
    @Operation(summary = "暂停/恢复促销活动", description = "暂停或恢复进行中的促销活动")
    public Result<Boolean> togglePause(
            @Parameter(description = "促销活动ID", required = true)
            @PathVariable @NotNull Long promotionId) {
        log.info("暂停/恢复促销活动请求，活动ID: {}", promotionId);

        boolean success = promotionService.togglePause(promotionId);
        return Result.success("操作成功", success);
    }

    /**
     * 结束促销活动
     */
    @PutMapping("/end/{promotionId}")
    @Operation(summary = "结束促销活动", description = "手动结束促销活动")
    public Result<Boolean> endPromotion(
            @Parameter(description = "促销活动ID", required = true)
            @PathVariable @NotNull Long promotionId) {
        log.info("结束促销活动请求，活动ID: {}", promotionId);

        boolean success = promotionService.endPromotion(promotionId);
        return Result.success("促销活动已结束", success);
    }

    /**
     * 用户参与促销活动
     */
    @PostMapping("/participate/{promotionId}")
    @Operation(summary = "参与促销活动", description = "用户参与促销活动，增加参与人数统计")
    public Result<Boolean> participatePromotion(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "促销活动ID", required = true)
            @PathVariable @NotNull Long promotionId) {
        log.info("用户参与促销活动，用户ID: {}, 活动ID: {}", userId, promotionId);

        boolean success = promotionService.participatePromotion(userId, promotionId);
        return Result.success("参与活动成功", success);
    }

    /**
     * 检查用户是否已参与营销活动
     */
    @GetMapping("/user-cart/check/{promotionId}")
    @Operation(summary = "检查用户是否已参与营销活动", description = "检查当前用户是否已参与指定营销活动")
    public Result<Boolean> checkUserParticipated(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Parameter(description = "营销活动ID", required = true)
            @PathVariable @NotNull Long promotionId) {
        log.info("检查用户是否已参与营销活动，用户ID: {}, 活动ID: {}", userId, promotionId);

        boolean hasParticipated = promotionUserCartService.hasUserParticipatedPromotion(userId, promotionId);
        return Result.success("查询成功", hasParticipated);
    }

    /**
     * 保存用户营销活动参与记录（绑定用户ID、营销活动ID和购物车商品ID）
     */
    @PostMapping("/user-cart")
    @Operation(summary = "保存用户营销活动参与记录", description = "保存用户参与营销活动的记录，绑定购物车商品。每个用户对同一活动只能参与一次")
    public Result<Long> savePromotionUserCart(
            @Parameter(description = "用户ID", required = true)
            @RequestHeader(value = "X-User-Id", required = false, defaultValue = "1") Long userId,
            @Valid @RequestBody PromotionUserCartCreateDTO createDTO) {
        log.info("保存用户营销活动参与记录，用户ID: {}, 数据: {}", userId, createDTO);

        try {
            Long recordId = promotionUserCartService.saveOrUpdatePromotionUserCart(
                userId,
                createDTO.getPromotionId(),
                createDTO.getProductId(),
                createDTO.getCartItemId(),
                createDTO.getQuantity(),
                createDTO.getOriginalPrice(),
                createDTO.getPromotionPrice(),
                createDTO.getDiscountAmount()
            );
            return Result.success("保存成功", recordId);
        } catch (com.naicha.hou.exception.BusinessException e) {
            // 如果是业务异常（如重复参与），直接抛出
            throw e;
        }
    }

    /**
     * 分页查询促销活动列表（用户端，不需要商户ID）
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询促销活动列表", description = "根据条件分页查询促销活动列表，商家端需要商户ID，用户端可选")
    public Result<IPage<PromotionDTO>> getPromotionPage(
            @Parameter(description = "商家ID（商家端必填，用户端可选）")
            @RequestHeader(value = "X-Merchant-Id", required = false) Long merchantId,
            @Valid @RequestBody PromotionQueryDTO queryDTO) {
        log.info("分页查询促销活动列表，商家ID: {}, 查询条件: {}", merchantId, queryDTO);

        // 如果请求头中有商户ID，设置到查询条件中；否则允许查询所有商户的活动
        if (merchantId != null) {
            queryDTO.setMerchantId(merchantId);
        }
        // 如果查询条件中没有设置状态，默认查询活跃状态的活动（用户端）
        if (queryDTO.getStatus() == null && merchantId == null) {
            queryDTO.setStatus("active");
        }

        IPage<PromotionDTO> result = promotionService.getPromotionList(queryDTO);
        return Result.success("查询成功", result);
    }
}

