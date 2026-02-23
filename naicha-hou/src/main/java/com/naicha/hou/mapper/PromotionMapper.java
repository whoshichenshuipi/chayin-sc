package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.PromotionDTO;
import com.naicha.hou.dto.PromotionQueryDTO;
import com.naicha.hou.entity.Promotion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 促销活动Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface PromotionMapper extends BaseMapper<Promotion> {

    /**
     * 分页查询促销活动列表
     *
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 促销活动列表
     */
    IPage<PromotionDTO> selectPromotionPage(Page<PromotionDTO> page, @Param("query") PromotionQueryDTO queryDTO);

    /**
     * 根据ID查询促销活动详情
     *
     * @param promotionId 促销活动ID
     * @return 促销活动详情
     */
    PromotionDTO selectPromotionDetail(@Param("promotionId") Long promotionId);
}

