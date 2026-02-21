package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.entity.MerchantConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商家配置Mapper接口
 */
@Mapper
public interface MerchantConfigMapper extends BaseMapper<MerchantConfig> {
    
    /**
     * 根据商家ID查询配置
     * 
     * @param merchantId 商家ID
     * @return 商家配置
     */
    MerchantConfig selectByMerchantId(@Param("merchantId") Long merchantId);
}
