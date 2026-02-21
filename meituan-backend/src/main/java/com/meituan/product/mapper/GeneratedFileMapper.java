package com.meituan.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.meituan.product.entity.GeneratedFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 生成文件 Mapper
 */
@Mapper
public interface GeneratedFileMapper extends BaseMapper<GeneratedFile> {
    
    /**
     * 查询商家的最近生成文件
     * 
     * @param merchantId 商家ID
     * @param limit 限制数量
     * @return 文件列表
     */
    @Select("SELECT * FROM generated_files WHERE merchant_id = #{merchantId} " +
            "AND (expires_at IS NULL OR expires_at > NOW()) " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    List<GeneratedFile> selectRecentByMerchantId(@Param("merchantId") Long merchantId, 
                                                   @Param("limit") Integer limit);
    
    /**
     * 增加下载次数
     * 
     * @param id 文件ID
     * @return 更新行数
     */
    @Update("UPDATE generated_files SET download_count = download_count + 1 WHERE id = #{id}")
    int incrementDownloadCount(@Param("id") Long id);
    
    /**
     * 删除过期文件记录
     * 
     * @param expiryTime 过期时间
     * @return 删除行数
     */
    @Select("SELECT * FROM generated_files WHERE expires_at < #{expiryTime}")
    List<GeneratedFile> selectExpiredFiles(@Param("expiryTime") LocalDateTime expiryTime);
}
