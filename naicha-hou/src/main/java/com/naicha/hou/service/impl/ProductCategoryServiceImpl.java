package com.naicha.hou.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.naicha.hou.common.ResultCode;
import com.naicha.hou.dto.ProductCategoryCreateDTO;
import com.naicha.hou.dto.ProductCategoryDTO;
import com.naicha.hou.entity.Product;
import com.naicha.hou.entity.ProductCategory;
import com.naicha.hou.exception.BusinessException;
import com.naicha.hou.mapper.ProductCategoryMapper;
import com.naicha.hou.mapper.ProductMapper;
import com.naicha.hou.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 *
 * @author naicha
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryMapper productCategoryMapper;
    private final ProductMapper productMapper;

    @Override
    public List<ProductCategoryDTO> getCategoryTree() {
        log.info("获取分类树");

        // 查询所有启用的分类
        List<ProductCategory> categories = productCategoryMapper.selectList(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getStatus, 1)
                        .orderByAsc(ProductCategory::getSort)
        );

        // 构建树形结构
        return buildCategoryTree(categories);
    }

    @Override
    public ProductCategoryDTO getCategoryById(Long id) {
        log.info("根据ID查询分类，ID: {}", id);

        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类不存在");
        }

        return convertToDTO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(ProductCategoryCreateDTO createDTO) {
        log.info("创建分类，名称: {}", createDTO.getName());

        // 检查父分类是否存在
        if (createDTO.getParentId() != null && createDTO.getParentId() != 0) {
            ProductCategory parent = productCategoryMapper.selectById(createDTO.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.VALIDATE_FAILED, "父分类不存在");
            }
        }

        // 检查分类名称是否重复
        checkNameDuplicate(createDTO.getName(), createDTO.getParentId(), null);

        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(createDTO, category);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        productCategoryMapper.insert(category);

        log.info("分类创建成功，ID: {}", category.getId());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(Long id, ProductCategoryCreateDTO createDTO) {
        log.info("更新分类，ID: {}, 名称: {}", id, createDTO.getName());

        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类不存在");
        }

        // 检查分类名称是否重复
        checkNameDuplicate(createDTO.getName(), category.getParentId(), id);

        // 检查是否可以设置自己为父分类
        if (createDTO.getParentId() != null && createDTO.getParentId().equals(id)) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "不能将自己设置为父分类");
        }

        BeanUtils.copyProperties(createDTO, category, "id", "createdAt");
        category.setUpdatedAt(LocalDateTime.now());

        productCategoryMapper.updateById(category);

        log.info("分类更新成功，ID: {}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        log.info("删除分类，ID: {}", id);

        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类不存在");
        }

        // 检查是否有子分类
        long childCount = productCategoryMapper.selectCount(
                new LambdaQueryWrapper<ProductCategory>()
                        .eq(ProductCategory::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "该分类下还有子分类，无法删除");
        }

        // 检查是否有商品
        int productCount = getProductCount(id);
        if (productCount > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "该分类下还有商品，无法删除");
        }

        productCategoryMapper.deleteById(id);

        log.info("分类删除成功，ID: {}", id);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        log.info("更新分类状态，ID: {}, 状态: {}", id, status);

        ProductCategory category = productCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类不存在");
        }

        category.setStatus(status);
        category.setUpdatedAt(LocalDateTime.now());
        productCategoryMapper.updateById(category);

        log.info("分类状态更新成功，ID: {}, 状态: {}", id, status);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSort(List<Long> ids) {
        log.info("更新分类排序，IDs: {}", ids);

        for (int i = 0; i < ids.size(); i++) {
            ProductCategory category = new ProductCategory();
            category.setId(ids.get(i));
            category.setSort(i + 1);
            productCategoryMapper.updateById(category);
        }

        log.info("分类排序更新成功");
        return true;
    }

    @Override
    public Integer getProductCount(Long categoryId) {
        return productMapper.selectCount(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getCategoryId, categoryId)
        ).intValue();
    }

    /**
     * 构建分类树
     */
    private List<ProductCategoryDTO> buildCategoryTree(List<ProductCategory> categories) {
        List<ProductCategoryDTO> rootNodes = new ArrayList<>();

        // 找出所有一级分类
        List<ProductCategory> level1Categories = categories.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .collect(Collectors.toList());

        for (ProductCategory level1Category : level1Categories) {
            ProductCategoryDTO dto = convertToDTO(level1Category);
            dto.setLevel(1);
            dto.setProductCount(getProductCount(level1Category.getId()));

            // 找出子分类
            List<ProductCategory> children = categories.stream()
                    .filter(c -> level1Category.getId().equals(c.getParentId()))
                    .collect(Collectors.toList());

            if (!children.isEmpty()) {
                List<ProductCategoryDTO> childrenDTOs = children.stream()
                        .map(child -> {
                            ProductCategoryDTO childDTO = convertToDTO(child);
                            childDTO.setLevel(2);
                            childDTO.setProductCount(getProductCount(child.getId()));
                            return childDTO;
                        })
                        .collect(Collectors.toList());
                dto.setChildren(childrenDTOs);
            }

            rootNodes.add(dto);
        }

        return rootNodes;
    }

    /**
     * 转换为DTO
     */
    private ProductCategoryDTO convertToDTO(ProductCategory category) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        BeanUtils.copyProperties(category, dto);
        dto.setStatusText(category.getStatus() == 1 ? "启用" : "禁用");
        return dto;
    }

    /**
     * 检查分类名称是否重复
     */
    private void checkNameDuplicate(String name, Long parentId, Long excludeId) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getName, name)
                .eq(parentId != null && parentId != 0, ProductCategory::getParentId, parentId);

        if (excludeId != null) {
            wrapper.ne(ProductCategory::getId, excludeId);
        }

        long count = productCategoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAILED, "分类名称已存在");
        }
    }
}

