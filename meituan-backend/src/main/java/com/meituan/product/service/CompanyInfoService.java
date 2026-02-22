package com.meituan.product.service;

import com.meituan.product.entity.CompanyInfo;
import com.meituan.product.mapper.CompanyInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 公司信息服务
 */
@Slf4j
@Service
public class CompanyInfoService {
    
    @Autowired
    private CompanyInfoMapper companyInfoMapper;
    
    /**
     * 获取公司信息（默认获取ID为1的记录）
     */
    public CompanyInfo getCompanyInfo() {
        CompanyInfo companyInfo = companyInfoMapper.selectById(1L);
        
        // 如果不存在，创建默认数据
        if (companyInfo == null) {
            companyInfo = createDefaultCompanyInfo();
        }
        
        return companyInfo;
    }
    
    /**
     * 创建默认公司信息
     */
    private CompanyInfo createDefaultCompanyInfo() {
        CompanyInfo companyInfo = new CompanyInfo();
        companyInfo.setId(1L);
        companyInfo.setCompanyName("云创智链科技");
        companyInfo.setSlogan("高效、智能的商品管理解决方案");
        companyInfo.setIntroPara1("专注于为企业提供高效的商品管理工具");
        companyInfo.setIntroPara2("支持批量导入、自动格式识别、一键上传");
        companyInfo.setIntroPara3("完全免费，立即下载使用");
        companyInfo.setVision("成为最受欢迎的商品管理工具");
        companyInfo.setMission("让商品管理更简单高效");
        companyInfo.setValues("用户至上，持续创新");
        companyInfo.setEmail("support@example.com");
        companyInfo.setPhone("400-123-4567");
        companyInfo.setAddress("中国");
        
        try {
            companyInfoMapper.insert(companyInfo);
            log.info("创建默认公司信息成功");
        } catch (Exception e) {
            log.error("创建默认公司信息失败", e);
        }
        
        return companyInfo;
    }
}
