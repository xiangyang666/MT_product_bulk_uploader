package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.dto.ProductStats;
import com.meituan.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.ResultHandler;

import java.util.List;

/**
 * 商品Mapper接口
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    
    /**
     * 批量插入商品
     * 
     * @param products 商品列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("list") List<Product> products);
    
    /**
     * 根据商家ID查询商品列表
     *
     * @param merchantId 商家ID
     * @return 商品列表
     */
    List<Product> selectByMerchantId(@Param("merchantId") Long merchantId);

    /**
     * 根据商家ID分页查询商品列表（支持搜索和日期筛选）
     *
     * @param merchantId 商家ID
     * @param keyword 搜索关键词（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 商品列表
     */
    List<Product> selectByMerchantIdPage(
        @Param("merchantId") Long merchantId,
        @Param("keyword") String keyword,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate,
        @Param("offset") Integer offset,
        @Param("limit") Integer limit
    );

    /**
     * 根据条件统计商品数量
     *
     * @param merchantId 商家ID
     * @param keyword 搜索关键词（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 商品数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_product WHERE merchant_id = #{merchantId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (product_name LIKE CONCAT('%', #{keyword}, '%') OR category_id LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "<if test='startDate != null and startDate != \"\"'>" +
            "AND DATE(created_time) &gt;= #{startDate} " +
            "</if>" +
            "<if test='endDate != null and endDate != \"\"'>" +
            "AND DATE(created_time) &lt;= #{endDate} " +
            "</if>" +
            "</script>")
    int countByMerchantId(
        @Param("merchantId") Long merchantId,
        @Param("keyword") String keyword,
        @Param("startDate") String startDate,
        @Param("endDate") String endDate
    );
    
    /**
     * 根据商家ID删除商品
     * 
     * @param merchantId 商家ID
     * @return 删除的记录数
     */
    int deleteByMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 获取商品统计信息
     * 
     * @param merchantId 商家ID
     * @return 统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as totalCount, " +
            "SUM(CASE WHEN status = 0 OR status IS NULL THEN 1 ELSE 0 END) as pendingCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as uploadedCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as failedCount " +
            "FROM t_product WHERE merchant_id = #{merchantId}")
    ProductStats getStats(@Param("merchantId") Long merchantId);
    
    /**
     * 获取最近导入的商品
     * 
     * @param merchantId 商家ID
     * @param limit 限制数量
     * @return 商品列表
     */
    @Select("SELECT * FROM t_product " +
            "WHERE merchant_id = #{merchantId} " +
            "ORDER BY created_time DESC " +
            "LIMIT #{limit}")
    List<Product> selectRecent(@Param("merchantId") Long merchantId, 
                               @Param("limit") int limit);
    
    /**
     * 流式查询所有商品（用于生成模板）
     * 使用ResultHandler避免一次性加载所有数据到内存
     * 
     * @param merchantId 商家ID
     * @param handler 结果处理器
     */
    @Select("SELECT * FROM t_product WHERE merchant_id = #{merchantId}")
    @Options(resultSetType = ResultSetType.FORWARD_ONLY, fetchSize = 1000)
    void streamAll(@Param("merchantId") Long merchantId, 
                   ResultHandler<Product> handler);
    
    /**
     * 批量更新商家所有商品的状态
     * 
     * @param merchantId 商家ID
     * @param status 新状态（0-待上传，1-已上传，2-失败）
     * @return 更新的记录数
     */
    @org.apache.ibatis.annotations.Update("UPDATE t_product SET status = #{status} WHERE merchant_id = #{merchantId}")
    int updateStatusByMerchantId(@Param("merchantId") Long merchantId, 
                                  @Param("status") Integer status);
}
