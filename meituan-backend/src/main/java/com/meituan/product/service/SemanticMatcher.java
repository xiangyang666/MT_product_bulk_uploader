package com.meituan.product.service;

import com.meituan.product.dto.FieldMappingRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 语义匹配器
 * 实现多种匹配算法来识别列名与字段的对应关系
 */
@Slf4j
@Component
public class SemanticMatcher {
    
    /**
     * 精确匹配
     * 列名完全等于规则中的exactMatches
     * 
     * @param columnName 列名
     * @param rule 映射规则
     * @return 是否匹配
     */
    public boolean exactMatch(String columnName, FieldMappingRule rule) {
        if (columnName == null || rule == null || rule.getExactMatches() == null) {
            return false;
        }
        
        for (String exactMatch : rule.getExactMatches()) {
            if (columnName.equals(exactMatch)) {
                log.debug("精确匹配成功: '{}' == '{}'", columnName, exactMatch);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 标准化匹配
     * 去除空格、标点、括号等特殊字符后进行匹配
     * 
     * @param columnName 列名
     * @param rule 映射规则
     * @return 是否匹配
     */
    public boolean normalizedMatch(String columnName, FieldMappingRule rule) {
        if (columnName == null || rule == null) {
            return false;
        }
        
        String normalizedColumn = normalizeString(columnName);
        
        // 检查精确匹配列表
        if (rule.getExactMatches() != null) {
            for (String exactMatch : rule.getExactMatches()) {
                String normalizedExact = normalizeString(exactMatch);
                if (normalizedColumn.equals(normalizedExact)) {
                    log.debug("标准化匹配成功: '{}' -> '{}' == '{}' -> '{}'", 
                        columnName, normalizedColumn, exactMatch, normalizedExact);
                    return true;
                }
            }
        }
        
        // 检查同义词列表
        if (rule.getSynonyms() != null) {
            for (String synonym : rule.getSynonyms()) {
                String normalizedSynonym = normalizeString(synonym);
                if (normalizedColumn.equals(normalizedSynonym)) {
                    log.debug("标准化匹配成功（同义词）: '{}' -> '{}' == '{}' -> '{}'", 
                        columnName, normalizedColumn, synonym, normalizedSynonym);
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 关键词匹配
     * 检查列名是否包含规则中的关键词
     * 
     * @param columnName 列名
     * @param rule 映射规则
     * @return 匹配得分（0-1），0表示不匹配
     */
    public double keywordMatch(String columnName, FieldMappingRule rule) {
        if (columnName == null || rule == null) {
            return 0.0;
        }
        
        String normalizedColumn = normalizeString(columnName);
        double maxScore = 0.0;
        
        // 1. 检查关键词组合（所有关键词都必须存在）
        if (rule.getKeywordCombinations() != null) {
            for (List<String> combination : rule.getKeywordCombinations()) {
                boolean allMatch = true;
                for (String keyword : combination) {
                    String normalizedKeyword = normalizeString(keyword);
                    if (!normalizedColumn.contains(normalizedKeyword)) {
                        allMatch = false;
                        break;
                    }
                }
                
                if (allMatch) {
                    // 关键词组合匹配，得分较高
                    double score = 0.8 + (combination.size() * 0.05); // 关键词越多，得分越高
                    score = Math.min(score, 0.95); // 最高0.95
                    log.debug("关键词组合匹配成功: '{}' 包含 {}, 得分: {}", 
                        columnName, combination, score);
                    maxScore = Math.max(maxScore, score);
                }
            }
        }
        
        // 2. 检查单个关键词
        if (rule.getKeywords() != null && !rule.getKeywords().isEmpty()) {
            int matchedCount = 0;
            for (String keyword : rule.getKeywords()) {
                String normalizedKeyword = normalizeString(keyword);
                if (normalizedColumn.contains(normalizedKeyword)) {
                    matchedCount++;
                }
            }
            
            if (matchedCount > 0) {
                // 匹配的关键词比例
                double ratio = (double) matchedCount / rule.getKeywords().size();
                double score = 0.7 + (ratio * 0.15); // 0.7-0.85之间
                log.debug("关键词匹配: '{}' 匹配了 {}/{} 个关键词, 得分: {}", 
                    columnName, matchedCount, rule.getKeywords().size(), score);
                maxScore = Math.max(maxScore, score);
            }
        }
        
        return maxScore;
    }
    
    /**
     * 模糊匹配
     * 使用Levenshtein距离计算字符串相似度
     * 
     * @param columnName 列名
     * @param rule 映射规则
     * @return 相似度得分（0-1），0表示完全不相似
     */
    public double fuzzyMatch(String columnName, FieldMappingRule rule) {
        if (columnName == null || rule == null) {
            return 0.0;
        }
        
        String normalizedColumn = normalizeString(columnName);
        double maxSimilarity = 0.0;
        
        // 与精确匹配列表比较
        if (rule.getExactMatches() != null) {
            for (String exactMatch : rule.getExactMatches()) {
                String normalizedExact = normalizeString(exactMatch);
                double similarity = calculateSimilarity(normalizedColumn, normalizedExact);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    log.debug("模糊匹配: '{}' vs '{}', 相似度: {}", 
                        columnName, exactMatch, similarity);
                }
            }
        }
        
        // 与同义词列表比较
        if (rule.getSynonyms() != null) {
            for (String synonym : rule.getSynonyms()) {
                String normalizedSynonym = normalizeString(synonym);
                double similarity = calculateSimilarity(normalizedColumn, normalizedSynonym);
                if (similarity > maxSimilarity) {
                    maxSimilarity = similarity;
                    log.debug("模糊匹配（同义词）: '{}' vs '{}', 相似度: {}", 
                        columnName, synonym, similarity);
                }
            }
        }
        
        // 模糊匹配的得分范围是0.5-0.8
        // 只有相似度超过0.6才认为是有效的模糊匹配
        if (maxSimilarity >= 0.6) {
            double score = 0.5 + (maxSimilarity - 0.6) * 0.75; // 映射到0.5-0.8范围
            return Math.min(score, 0.8);
        }
        
        return 0.0;
    }
    
    /**
     * 标准化字符串
     * 去除空格、标点、括号等特殊字符，转换为小写
     * 
     * @param str 原始字符串
     * @return 标准化后的字符串
     */
    private String normalizeString(String str) {
        if (str == null) {
            return "";
        }
        
        // 去除所有空白字符
        String normalized = str.replaceAll("\\s+", "");
        
        // 去除常见标点符号和括号
        normalized = normalized.replaceAll("[()（）\\[\\]【】{}「」『』<>《》、，。；：！？*]", "");
        
        // 转换为小写（对英文有效）
        normalized = normalized.toLowerCase();
        
        return normalized;
    }
    
    /**
     * 计算两个字符串的相似度
     * 使用Levenshtein距离算法
     * 
     * @param s1 字符串1
     * @param s2 字符串2
     * @return 相似度（0-1），1表示完全相同
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }
        
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        int distance = levenshteinDistance(s1, s2);
        int maxLength = Math.max(s1.length(), s2.length());
        
        if (maxLength == 0) {
            return 1.0;
        }
        
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * 计算Levenshtein距离
     * 表示将一个字符串转换为另一个字符串所需的最少编辑操作次数
     * 
     * @param s1 字符串1
     * @param s2 字符串2
     * @return 编辑距离
     */
    private int levenshteinDistance(String s1, String s2) {
        int len1 = s1.length();
        int len2 = s2.length();
        
        // 创建距离矩阵
        int[][] dp = new int[len1 + 1][len2 + 1];
        
        // 初始化第一行和第一列
        for (int i = 0; i <= len1; i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= len2; j++) {
            dp[0][j] = j;
        }
        
        // 动态规划计算距离
        for (int i = 1; i <= len1; i++) {
            for (int j = 1; j <= len2; j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                
                dp[i][j] = Math.min(
                    Math.min(
                        dp[i - 1][j] + 1,      // 删除
                        dp[i][j - 1] + 1       // 插入
                    ),
                    dp[i - 1][j - 1] + cost    // 替换
                );
            }
        }
        
        return dp[len1][len2];
    }
}
