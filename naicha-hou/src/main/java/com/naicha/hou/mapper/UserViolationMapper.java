package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.UserViolation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户违规记录Mapper接口
 *
 * @author naicha
 * @since 2024-01-01
 */
@Mapper
public interface UserViolationMapper extends BaseMapper<UserViolation> {

    /**
     * 根据用户ID查询违规记录列表
     *
     * @param userId 用户ID
     * @return 违规记录列表
     */
    List<UserViolation> selectByUserId(@Param("userId") Long userId);
}

