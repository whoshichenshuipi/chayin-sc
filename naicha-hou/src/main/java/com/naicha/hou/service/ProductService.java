package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.ProductCreateDTO;
import com.naicha.hou.dto.ProductDTO;
import com.naicha.hou.dto.ProductQueryDTO;

/**
 * 商品服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface ProductService {

    /**
     * 创建商品
     *
     * @param merchantId 商家ID
     * @param createDTO 商品创建DTO
     * @return 商品ID
     */
    Long createProduct(Long merchantId, ProductCreateDTO createDTO);

    /**
     * 更新商品
     *
     * @param productId 商品ID
     * @param createDTO 商品创建DTO
     * @return 是否成功
     */
    boolean updateProduct(Long productId, ProductCreateDTO createDTO);

    /**
     * 删除商品
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long productId);

    /**
     * 根据ID查询商品详情
     *
     * @param productId 商品ID
     * @return 商品详情DTO
     */
    ProductDTO getProductById(Long productId);

    /**
     * 分页查询商品列表
     *
     * @param queryDTO 查询条件
     * @return 分页数据
     */
    IPage<ProductDTO> getProductList(ProductQueryDTO queryDTO);

    /**
     * 上架商品
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean onSale(Long productId);

    /**
     * 下架商品
     *
     * @param productId 商品ID
     * @return 是否成功
     */
    boolean offSale(Long productId);

    /**
     * 调整库存
     *
     * @param productId 商品ID
     * @param stock 调整后的库存
     * @return 是否成功
     */
    boolean adjustStock(Long productId, Integer stock);

    /**
     * 更新商品状态
     *
     * @param productId 商品ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateStatus(Long productId, Integer status);
}

