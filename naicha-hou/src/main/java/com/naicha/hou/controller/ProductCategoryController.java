package com.naicha.hou.controller;

import com.naicha.hou.common.Result;
import com.naicha.hou.dto.ProductCategoryCreateDTO;
import com.naicha.hou.dto.ProductCategoryDTO;
import com.naicha.hou.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Validated
@Tag(name = "商品分类", description = "商品分类相关接口")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    /**
     * 获取商品分类列表（树形结构）
     */
    @GetMapping("/tree")
    @Operation(summary = "获取商品分类树", description = "返回树形结构的分类列表")
    public Result<List<ProductCategoryDTO>> getCategoryTree() {
        log.info("获取商品分类树");

        List<ProductCategoryDTO> categoryTree = productCategoryService.getCategoryTree();
        return Result.success("获取成功", categoryTree);
    }

    /**
     * 根据ID查询分类
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情", description = "根据分类ID查询分类详情")
    public Result<ProductCategoryDTO> getCategoryById(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull @Positive Long id) {
        log.info("查询分类详情，ID: {}", id);

        ProductCategoryDTO category = productCategoryService.getCategoryById(id);
        return Result.success("查询成功", category);
    }

    /**
     * 创建分类
     */
    @PostMapping
    @Operation(summary = "创建分类", description = "创建新的商品分类")
    public Result<Long> createCategory(@Valid @RequestBody ProductCategoryCreateDTO createDTO) {
        log.info("创建分类，名称: {}", createDTO.getName());

        Long id = productCategoryService.createCategory(createDTO);
        return Result.success("分类创建成功", id);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新分类", description = "更新分类信息")
    public Result<Boolean> updateCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody ProductCategoryCreateDTO createDTO) {
        log.info("更新分类，ID: {}", id);

        boolean success = productCategoryService.updateCategory(id, createDTO);
        return Result.success("分类更新成功", success);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类", description = "删除分类（有商品或子分类则无法删除）")
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull @Positive Long id) {
        log.info("删除分类，ID: {}", id);

        boolean success = productCategoryService.deleteCategory(id);
        return Result.success("分类删除成功", success);
    }

    /**
     * 更新分类状态
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新分类状态", description = "启用或禁用分类")
    public Result<Boolean> updateStatus(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull @Positive Long id,
            @Parameter(description = "状态", required = true) @RequestParam @NotNull Integer status) {
        log.info("更新分类状态，ID: {}, 状态: {}", id, status);

        boolean success = productCategoryService.updateStatus(id, status);
        return Result.success("状态更新成功", success);
    }

    /**
     * 更新分类排序
     */
    @PutMapping("/sort")
    @Operation(summary = "更新分类排序", description = "批量更新分类排序")
    public Result<Boolean> updateSort(@RequestBody List<Long> ids) {
        log.info("更新分类排序，IDs: {}", ids);

        boolean success = productCategoryService.updateSort(ids);
        return Result.success("排序更新成功", success);
    }

    /**
     * 获取分类下的商品数量
     */
    @GetMapping("/{id}/product-count")
    @Operation(summary = "获取分类商品数量", description = "获取分类下的商品数量")
    public Result<Integer> getProductCount(
            @Parameter(description = "分类ID", required = true) @PathVariable @NotNull @Positive Long id) {
        log.info("获取分类商品数量，ID: {}", id);

        Integer count = productCategoryService.getProductCount(id);
        return Result.success("获取成功", count);
    }
}

