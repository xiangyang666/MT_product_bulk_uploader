package com.meituan.product.controller;

import com.meituan.product.entity.CompanyInfo;
import com.meituan.product.service.CompanyInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 公司信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/company")
public class CompanyInfoController {
    
    @Autowired
    private CompanyInfoService companyInfoService;
    
    /**
     * 获取公司信息
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCompanyInfo() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            CompanyInfo companyInfo = companyInfoService.getCompanyInfo();
            
            response.put("code", 200);
            response.put("message", "获取成功");
            response.put("data", companyInfo);
            
            log.info("获取公司信息成功");
            
        } catch (Exception e) {
            log.error("获取公司信息失败", e);
            response.put("code", 500);
            response.put("message", "获取失败: " + e.getMessage());
            response.put("data", null);
        }
        
        return ResponseEntity.ok(response);
    }
}
