package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.entity.CompanyInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公司信息 Mapper
 */
@Mapper
public interface CompanyInfoMapper extends BaseMapper<CompanyInfo> {
}
