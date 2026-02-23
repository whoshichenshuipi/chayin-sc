package com.naicha.hou.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.naicha.hou.dto.UserFeedbackAssignDTO;
import com.naicha.hou.dto.UserFeedbackCompleteDTO;
import com.naicha.hou.dto.UserFeedbackDTO;
import com.naicha.hou.dto.UserFeedbackQueryDTO;

/**
 * 用户反馈服务接口
 *
 * @author naicha
 * @since 2024-01-01
 */
public interface UserFeedbackService {

    /**
     * 分页查询用户反馈列表
     *
     * @param queryDTO 查询条件
     * @return 分页数据
     */
    IPage<UserFeedbackDTO> getFeedbackPage(UserFeedbackQueryDTO queryDTO);

    /**
     * 根据反馈ID获取反馈详情
     *
     * @param feedbackId 反馈ID
     * @return 反馈详情
     */
    UserFeedbackDTO getFeedbackDetail(Long feedbackId);

    /**
     * 分配反馈处理
     *
     * @param assignDTO 分配信息
     * @return 是否成功
     */
    boolean assignFeedback(UserFeedbackAssignDTO assignDTO);

    /**
     * 完成反馈处理
     *
     * @param completeDTO 完成信息
     * @return 是否成功
     */
    boolean completeFeedback(UserFeedbackCompleteDTO completeDTO);
}

