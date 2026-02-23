package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.PromotionCreateDTO;
import com.naicha.hou.dto.PromotionDTO;
import com.naicha.hou.dto.PromotionQueryDTO;

/**
 * 促销活动服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface PromotionService {

    /**
     * 创建促销活动
     *
     * @param merchantId 商家ID
     * @param createDTO 促销活动创建DTO
     * @return 促销活动ID
     */
    Long createPromotion(Long merchantId, PromotionCreateDTO createDTO);

    /**
     * 更新促销活动
     *
     * @param promotionId 促销活动ID
     * @param createDTO 促销活动创建DTO
     * @return 是否成功
     */
    boolean updatePromotion(Long promotionId, PromotionCreateDTO createDTO);

    /**
     * 删除促销活动
     *
     * @param promotionId 促销活动ID
     * @return 是否成功
     */
    boolean deletePromotion(Long promotionId);

    /**
     * 根据ID查询促销活动详情
     *
     * @param promotionId 促销活动ID
     * @return 促销活动详情DTO
     */
    PromotionDTO getPromotionById(Long promotionId);

    /**
     * 分页查询促销活动列表
     *
     * @param queryDTO 查询条件
     * @return 分页数据
     */
    IPage<PromotionDTO> getPromotionList(PromotionQueryDTO queryDTO);

    /**
     * 暂停/恢复促销活动
     *
     * @param promotionId 促销活动ID
     * @return 是否成功
     */
    boolean togglePause(Long promotionId);

    /**
     * 结束促销活动
     *
     * @param promotionId 促销活动ID
     * @return 是否成功
     */
    boolean endPromotion(Long promotionId);

    /**
     * 更新促销活动状态（自动更新状态，基于时间）
     *
     * @param promotionId 促销活动ID
     * @return 是否成功
     */
    boolean updatePromotionStatus(Long promotionId);

    /**
     * 用户参与促销活动
     *
     * @param userId 用户ID
     * @param promotionId 促销活动ID
     * @return 是否成功
     */
    boolean participatePromotion(Long userId, Long promotionId);
}

