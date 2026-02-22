package com.jiwu.api.chat.common.utils.discover;

import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;

/**
 * 性能测试类
 * @author kiwi233
 */
public class PerformanceTest {
    
    public static void main(String[] args) {
        // 测试URL
        String[] testUrls = {
            "https://www.baidu.com",
            "https://github.com",
            "https://stackoverflow.com",
            "https://www.zhihu.com",
            "https://www.bilibili.com"
        };
        
        CommonUrlDiscover commonDiscover = new CommonUrlDiscover();
        
        System.out.println("=== 性能测试开始 ===");
        
        // 预热
        System.out.println("预热阶段...");
        for (int i = 0; i < 3; i++) {
            for (String url : testUrls) {
                commonDiscover.getContent(url);
            }
        }
        
        // 性能测试
        System.out.println("性能测试阶段...");
        long totalTime = 0;
        int iterations = 10;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            
            for (String url : testUrls) {
                try {
                    UrlInfoDTO result = commonDiscover.getContent(url);
                    if (result != null) {
                        // 验证结果不为空
                        if (result.getTitle() != null) {
                            // 正常处理
                        }
                    }
                } catch (Exception e) {
                    System.err.println("处理URL出错: " + url + ", 错误: " + e.getMessage());
                }
            }
            
            long endTime = System.currentTimeMillis();
            long iterationTime = endTime - startTime;
            totalTime += iterationTime;
            
            System.out.println("第" + (i + 1) + "次迭代耗时: " + iterationTime + "ms");
        }
        
        double averageTime = (double) totalTime / iterations;
        System.out.println("=== 性能测试结果 ===");
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均耗时: " + String.format("%.2f", averageTime) + "ms");
        System.out.println("每个URL平均耗时: " + String.format("%.2f", averageTime / testUrls.length) + "ms");
    }
}
