package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.ProductSpec;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品规格Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface ProductSpecMapper extends BaseMapper<ProductSpec> {

    /**
     * 根据商品ID查询规格列表
     *
     * @param productId 商品ID
     * @return 规格列表
     */
    List<ProductSpec> selectByProductId(@Param("productId") Long productId);

    /**
     * 根据规格类型查询规格列表
     *
     * @param productId 商品ID
     * @param specType 规格类型
     * @return 规格列表
     */
    List<ProductSpec> selectBySpecType(@Param("productId") Long productId, @Param("specType") String specType);
}

