package com.naicha.hou.service;

import com.naicha.hou.dto.ProductCategoryCreateDTO;
import com.naicha.hou.dto.ProductCategoryDTO;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface ProductCategoryService {

    /**
     * 获取分类树
     *
     * @return 分类树
     */
    List<ProductCategoryDTO> getCategoryTree();

    /**
     * 根据ID查询分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    ProductCategoryDTO getCategoryById(Long id);

    /**
     * 创建分类
     *
     * @param createDTO 分类创建DTO
     * @return 分类ID
     */
    Long createCategory(ProductCategoryCreateDTO createDTO);

    /**
     * 更新分类
     *
     * @param id 分类ID
     * @param createDTO 分类更新DTO
     * @return 是否成功
     */
    boolean updateCategory(Long id, ProductCategoryCreateDTO createDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 更新分类状态
     *
     * @param id 分类ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 更新分类排序
     *
     * @param ids 分类ID列表（按排序顺序）
     * @return 是否成功
     */
    boolean updateSort(List<Long> ids);

    /**
     * 获取分类下的商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    Integer getProductCount(Long categoryId);
}

