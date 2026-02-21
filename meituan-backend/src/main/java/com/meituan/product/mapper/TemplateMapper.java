package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.entity.Template;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模板Mapper接口
 */
@Mapper
public interface TemplateMapper extends BaseMapper<Template> {
    
    /**
     * 根据商家ID查询模板列表
     * 
     * @param merchantId 商家ID
     * @return 模板列表
     */
    List<Template> selectByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据商家ID和模板类型查询模板
     * 
     * @param merchantId 商家ID
     * @param templateType 模板类型
     * @return 模板列表
     */
    List<Template> selectByMerchantIdAndType(
        @Param("merchantId") Long merchantId, 
        @Param("templateType") String templateType
    );
}
