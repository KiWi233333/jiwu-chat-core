package com.jiwu.api.chat.common.utils.discover;

import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;

/**
 * URL解析器功能测试
 *
 * @author kiwi233
 */
public class UrlDiscoverTest {
    /**
     * 压力测试：多线程并发解析URL
     */
    public static void stressTest(PrioritizedUrlDiscover discover, String[] testUrls, int threadCount, int loopCount) {
        System.out.println("=== URL解析器压力测试 ===");
        final int[] success = {0};
        final int[] fail = {0};
        Thread[] threads = new Thread[threadCount];
        long startAll = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < loopCount; j++) {
                    for (String url : testUrls) {
                        try {
                            UrlInfoDTO result = discover.getContent(url);
                            if (result != null) {
                                synchronized (success) { success[0]++; }
                            } else {
                                synchronized (fail) { fail[0]++; }
                            }
                        } catch (Exception e) {
                            synchronized (fail) { fail[0]++; }
                        }
                    }
                }
            });
            threads[i].start();
        }
        for (Thread t : threads) {
            try { t.join(); } catch (InterruptedException ignored) {}
        }
        long endAll = System.currentTimeMillis();
        System.out.println("压力测试完成，总耗时: " + (endAll - startAll) + " ms");
        System.out.println("成功次数: " + success[0]);
        System.out.println("失败次数: " + fail[0]);
    }

    public static void main(String[] args) {
        // 测试不同类型的URL
        String[] testUrls = {
            "https://www.baidu.com",
            "https://www.taobao.com",
            "https://www.jd.com",
            "https://www.tmall.com",
            "https://www.sina.com.cn",
            "https://www.qq.com",
            "https://www.163.com",
            "https://www.sohu.com",
            "https://www.ifeng.com",
            "https://www.bilibili.com",
            "https://www.zhihu.com",
            "https://www.douban.com",
            "https://www.csdn.net",
            "https://www.yuque.com",
            "https://www.toutiao.com",
            "https://www.ctrip.com",
            "https://www.meituan.com",
            "https://www.dianping.com",
            "https://www.autohome.com.cn",
            "https://www.58.com",
            "https://www.sougou.com",
            "https://www.51job.com",
            "https://www.gmw.cn"
        };

        PrioritizedUrlDiscover discover = new PrioritizedUrlDiscover();


        System.out.println("=== URL解析器测试 ===");
        long totalStart = System.currentTimeMillis();
        for (String url : testUrls) {
            System.out.println("\n测试URL: " + url);
            System.out.println("-------------------");

            long start = System.currentTimeMillis();
            try {
                UrlInfoDTO result = discover.getContent(url);
                long end = System.currentTimeMillis();
                System.out.println("耗时: " + (end - start) + " ms");
                if (result != null) {
//                    printUrlInfo(result);
                } else {
                    System.out.println("无法解析该URL");
                }
            } catch (Exception e) {
                long end = System.currentTimeMillis();
                System.out.println("耗时: " + (end - start) + " ms");
                System.out.println("解析出错: " + e.getMessage());
            }
        }
        long totalEnd = System.currentTimeMillis();
        System.out.println("全部URL测试总耗时: " + (totalEnd - totalStart) + " ms");

        // 压力测试：10线程，每线程循环2次（可根据需要调整）
        stressTest(discover, testUrls, 10, 2);
    }

    private static void printUrlInfo(UrlInfoDTO urlInfo) {
        System.out.println("标题: " + urlInfo.getTitle());
        System.out.println("描述: " + urlInfo.getDescription());
        System.out.println("图片: " + urlInfo.getImage());
        System.out.println("图标: " + urlInfo.getIcon());
        System.out.println("网站名: " + urlInfo.getSiteName());
        System.out.println("URL: " + urlInfo.getUrl());
        System.out.println("类型: " + urlInfo.getType());
        System.out.println("作者: " + urlInfo.getAuthor());
        System.out.println("发布者: " + urlInfo.getPublisher());
        System.out.println("语言: " + urlInfo.getLanguage());
    }
}
