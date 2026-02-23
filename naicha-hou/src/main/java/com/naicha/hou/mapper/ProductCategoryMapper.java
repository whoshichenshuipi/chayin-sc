package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {
}

