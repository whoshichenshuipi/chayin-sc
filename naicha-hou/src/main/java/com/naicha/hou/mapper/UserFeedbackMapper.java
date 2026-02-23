package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.naicha.hou.dto.UserFeedbackDTO;
import com.naicha.hou.dto.UserFeedbackQueryDTO;
import com.naicha.hou.entity.UserFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户反馈Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface UserFeedbackMapper extends BaseMapper<UserFeedback> {

    /**
     * 分页查询用户反馈列表（包含用户信息）
     *
     * @param page 分页对象
     * @param query 查询条件
     * @return 分页数据
     */
    IPage<UserFeedbackDTO> selectFeedbackPage(Page<UserFeedbackDTO> page, @Param("query") UserFeedbackQueryDTO query);

    /**
     * 根据反馈ID查询反馈详情（包含用户信息）
     *
     * @param feedbackId 反馈ID
     * @return 反馈详情
     */
    UserFeedbackDTO selectFeedbackDetail(@Param("feedbackId") Long feedbackId);
}

