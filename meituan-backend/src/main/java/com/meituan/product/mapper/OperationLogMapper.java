package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    /**
     * 根据商家ID查询操作日志
     * 
     * @param merchantId 商家ID
     * @return 操作日志列表
     */
    List<OperationLog> selectByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 根据操作类型查询日志
     * 
     * @param merchantId 商家ID
     * @param operationType 操作类型
     * @return 操作日志列表
     */
    List<OperationLog> selectByOperationType(
        @Param("merchantId") Long merchantId, 
        @Param("operationType") String operationType
    );
    
    /**
     * 获取最近操作记录
     * 
     * @param merchantId 商家ID
     * @param operationType 操作类型（可选）
     * @param limit 限制数量
     * @return 操作日志列表
     */
    @Select("<script>" +
            "SELECT * FROM t_operation_log " +
            "WHERE merchant_id = #{merchantId} " +
            "<if test='operationType != null and operationType != \"\"'>" +
            "AND operation_type = #{operationType} " +
            "</if>" +
            "ORDER BY created_time DESC " +
            "LIMIT #{limit}" +
            "</script>")
    List<OperationLog> selectRecent(@Param("merchantId") Long merchantId,
                                    @Param("operationType") String operationType,
                                    @Param("limit") int limit);
}
