package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.entity.AppVersion;
import org.apache.ibatis.annotations.Mapper;

/**
 * 应用版本 Mapper 接口
 */
@Mapper
public interface AppVersionMapper extends BaseMapper<AppVersion> {
}
