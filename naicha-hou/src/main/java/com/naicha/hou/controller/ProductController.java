package com.naicha.hou.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.common.Result;
import com.naicha.hou.dto.ProductCreateDTO;
import com.naicha.hou.dto.ProductDTO;
import com.naicha.hou.dto.ProductQueryDTO;
import com.naicha.hou.enums.ProductStatus;
import com.naicha.hou.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/**
 * 商品管理控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "商品管理", description = "商品增删改查、上架下架等接口")
public class ProductController {

    private final ProductService productService;

    /**
     * 创建商品
     */
    @PostMapping("/create")
    @Operation(summary = "创建商品", description = "商家创建新商品，包含基本信息、规格、库存、价格等")
    public Result<Long> createProduct(
            @Parameter(description = "商家ID", required = true) @RequestParam @NotNull Long merchantId,
            @Valid @RequestBody ProductCreateDTO createDTO) {
        log.info("创建商品请求，商家ID: {}", merchantId);

        Long productId = productService.createProduct(merchantId, createDTO);
        return Result.success("商品创建成功", productId);
    }

    /**
     * 更新商品
     */
    @PutMapping("/update/{productId}")
    @Operation(summary = "更新商品", description = "更新商品信息")
    public Result<Boolean> updateProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable @NotNull Long productId,
            @Valid @RequestBody ProductCreateDTO createDTO) {
        log.info("更新商品请求，商品ID: {}", productId);

        boolean success = productService.updateProduct(productId, createDTO);
        return Result.success("商品更新成功", success);
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{productId}")
    @Operation(summary = "删除商品", description = "逻辑删除商品")
    public Result<Boolean> deleteProduct(
            @Parameter(description = "商品ID", required = true) @PathVariable @NotNull Long productId) {
        log.info("删除商品请求，商品ID: {}", productId);

        boolean success = productService.deleteProduct(productId);
        return Result.success("商品删除成功", success);
    }

    /**
     * 查询商品详情
     */
    @GetMapping("/detail/{productId}")
    @Operation(summary = "查询商品详情", description = "根据商品ID查询商品详情")
    public Result<ProductDTO> getProductDetail(
            @Parameter(description = "商品ID", required = true) @PathVariable @NotNull Long productId) {
        log.info("查询商品详情，商品ID: {}", productId);

        ProductDTO product = productService.getProductById(productId);
        return Result.success("查询成功", product);
    }

    /**
     * 分页查询商品列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询商品列表", description = "支持按商家ID、分类、状态等条件筛选")
    public Result<IPage<ProductDTO>> getProductList(@Valid ProductQueryDTO queryDTO) {
        log.info("查询商品列表，查询条件: {}", queryDTO);

        IPage<ProductDTO> productPage = productService.getProductList(queryDTO);
        return Result.success("查询成功", productPage);
    }

    /**
     * 上架商品
     */
    @PutMapping("/on-sale/{productId}")
    @Operation(summary = "上架商品", description = "将商品状态改为上架")
    public Result<Boolean> onSale(
            @Parameter(description = "商品ID", required = true) @PathVariable @NotNull Long productId) {
        log.info("上架商品请求，商品ID: {}", productId);

        boolean success = productService.onSale(productId);
        return Result.success("商品上架成功", success);
    }

    /**
     * 下架商品
     */
    @PutMapping("/off-sale/{productId}")
    @Operation(summary = "下架商品", description = "将商品状态改为下架")
    public Result<Boolean> offSale(
            @Parameter(description = "商品ID", required = true) @PathVariable @NotNull Long productId) {
        log.info("下架商品请求，商品ID: {}", productId);

        boolean success = productService.offSale(productId);
        return Result.success("商品下架成功", success);
    }

    /**
     * 调整库存
     */
    @PutMapping("/adjust-stock/{productId}")
    @Operation(summary = "调整库存", description = "调整商品库存数量")
    public Result<Boolean> adjustStock(
            @Parameter(description = "商品ID", required = true) @PathVariable @NotNull Long productId,
            @Parameter(description = "库存数量", required = true) @RequestParam @NotNull @Positive Integer stock) {
        log.info("调整库存请求，商品ID: {}, 库存: {}", productId, stock);

        boolean success = productService.adjustStock(productId, stock);
        return Result.success("库存调整成功", success);
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/status/{productId}")
    @Operation(summary = "更新商品状态", description = "更新商品状态，支持所有状态转换")
    public Result<Boolean> updateStatus(
            @Parameter(description = "商品ID", required = true) @PathVariable @NotNull Long productId,
            @Parameter(description = "商品状态", required = true, example = "1") 
            @RequestParam @NotNull @Positive Integer status) {
        log.info("更新商品状态请求，商品ID: {}, 状态: {}", productId, status);

        boolean success = productService.updateStatus(productId, status);
        return Result.success("状态更新成功", success);
    }

    /**
     * 获取商品状态列表
     */
    @GetMapping("/status/list")
    @Operation(summary = "获取商品状态列表", description = "返回所有可用的商品状态及状态码")
    public Result<List<StatusVO>> getStatusList() {
        log.info("获取商品状态列表");

        List<StatusVO> statusList = List.of(
            new StatusVO(ProductStatus.OFFLINE.getCode(), ProductStatus.OFFLINE.getDescription()),
            new StatusVO(ProductStatus.ONLINE.getCode(), ProductStatus.ONLINE.getDescription()),
            new StatusVO(ProductStatus.PRESALE.getCode(), ProductStatus.PRESALE.getDescription()),
            new StatusVO(ProductStatus.AUDITING.getCode(), ProductStatus.AUDITING.getDescription()),
            new StatusVO(ProductStatus.AUDIT_FAILED.getCode(), ProductStatus.AUDIT_FAILED.getDescription())
        );

        return Result.success("获取成功", statusList);
    }

    /**
     * 状态VO
     */
    @lombok.Data
    public static class StatusVO {
        private Integer code;
        private String description;

        public StatusVO(Integer code, String description) {
            this.code = code;
            this.description = description;
        }
    }
}

