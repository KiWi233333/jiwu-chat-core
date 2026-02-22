package com.jiwu.api.common.util.service.translation;

import cn.hutool.core.collection.ListUtil;
import com.jiwu.api.common.exception.BusinessException;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateRequest;
import com.tencentcloudapi.tmt.v20180321.models.TextTranslateResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class TranslationUtil {

    @Value("${tencent.secretId}")
    private String secretId;

    @Value("${tencent.secretKey}")
    private String secretKey;

    private TmtClient tmtClient;
    private static final String DEFAULT_REGION = "ap-guangzhou";


    @PostConstruct
    public void initClient() {
        /**
         * 地域列表
         * 亚太东南（曼谷）	    ap-bangkok
         * 华北地区（北京）	    ap-beijing
         * 西南地区（成都）	    ap-chengdu
         * 西南地区（重庆）	    ap-chongqing
         * 华南地区（广州）	    ap-guangzhou
         * 港澳台地区（中国香港）	ap-hongkong
         * 亚太南部（孟买）	    ap-mumbai
         * 亚太东北（首尔）	    ap-seoul
         * 华东地区（上海）	    ap-shanghai
         * 华东地区（上海金融）	ap-shanghai-fsi
         * 华南地区（深圳金融）	ap-shenzhen-fsi
         * 亚太东南（新加坡）	    ap-singapore
         * 亚太东北（东京）	    ap-tokyo
         * 欧洲地区（法兰克福）	eu-frankfurt
         * 美国东部（弗吉尼亚）	na-ashburn
         * 美国西部（硅谷）	    na-siliconvalley
         * 北美地区（多伦多）	    na-toronto
         */
        tmtClient = new TmtClient(new Credential(secretId, secretKey), DEFAULT_REGION);
    }

    /**
     * text 需要翻译的文本
     * sourceLang 翻译文本的语种
     * targetLang 目标语种
     */
    public TextTranslateResponse translateText(String text, String sourceLang, String targetLang) {
        try {
            TextTranslateRequest req = new TextTranslateRequest();
            req.setSourceText(text);
            req.setSource(sourceLang);
            req.setTarget(targetLang);
            req.setProjectId(0L);
            return tmtClient.TextTranslate(req);
        } catch (TencentCloudSDKException e) {
            log.error("翻译失败：{}", e.getMessage());
            throw new BusinessException("翻译失败，请稍后重试！");
        }
    }


    // 翻译工具类型
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public enum TranslationToolEnums {
        TENCENT(1, "腾讯翻译"),
        AI(2, "AI翻译");

        private Integer value;
        private String label;

        private static final HashMap<Integer, TranslationToolVO> tools = new HashMap<>();

        static {
            for (TranslationToolEnums enums : TranslationToolEnums.values()) {
                tools.put(enums.getValue(), new TranslationToolVO(enums.getLabel(), enums.getValue()));
            }
        }

        // 获取工具列表
        public static List<TranslationToolVO> getTools() {
            return ListUtil.toList(tools.values());
        }

        // 根据value获取label
        public static TranslationToolVO getLabelByValue(Integer value) {
            return tools.get(value);
        }
    }
}
