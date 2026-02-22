package com.jiwu.api.common.util.service.translation;

/**
 * AI翻译服务接口
 * 用于解耦res模块和ai模块之间的循环依赖
 * 
 * @author Kiwi23333
 */
public interface AiTranslationService {
    
    /**
     * 同步翻译文本
     * 
     * @param text 待翻译文本
     * @param apiUrl API地址
     * @param apiKey API密钥
     * @param model 模型名称
     * @param maxTokens 最大token数
     * @return 翻译结果
     */
    String translateSync(String text, String apiUrl, String apiKey, String model, Integer maxTokens);
    
    /**
     * 流式翻译文本
     * 
     * @param text 待翻译文本
     * @param apiUrl API地址
     * @param apiKey API密钥
     * @param model 模型名称
     * @param maxTokens 最大token数
     * @param callback 流式回调接口
     */
    void translateStream(String text, String apiUrl, String apiKey, String model, Integer maxTokens, StreamCallback callback);
    
    /**
     * 流式翻译回调接口
     */
    @FunctionalInterface
    interface StreamCallback {
        /**
         * 接收翻译内容片段
         * 
         * @param content 内容片段
         * @param isEnd 是否结束
         */
        void onContent(String content, boolean isEnd);
    }
}

