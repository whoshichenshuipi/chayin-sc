package com.naicha.hou.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.naicha.hou.entity.SystemLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统日志Mapper接口
 *
 * @author naicha
 * @since 2024-01-25
 */
@Mapper
public interface SystemLogMapper extends BaseMapper<SystemLog> {
}

