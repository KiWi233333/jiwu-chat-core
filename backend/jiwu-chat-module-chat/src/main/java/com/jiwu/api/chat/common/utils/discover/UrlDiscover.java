package com.jiwu.api.chat.common.utils.discover;

import cn.hutool.core.date.StopWatch;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import org.jsoup.nodes.Document;

import jakarta.annotation.Nullable;
import java.util.Map;

/**
 * @author kiwi233
 */
public interface UrlDiscover {


    @Nullable
    Map<String, UrlInfoDTO> getUrlContentMap(String content);

    @Nullable
    UrlInfoDTO getContent(String url);

    @Nullable
    String getTitle(Document document);

    @Nullable
    String getDescription(Document document);

    @Nullable
    String getImage(String url, Document document);

    @Nullable
    String getIcon(String url, Document document);

    @Nullable
    String getSiteName(Document document);

    @Nullable
    String getType(Document document);

    @Nullable
    String getAuthor(Document document);

    @Nullable
    String getPublisher(Document document);

    @Nullable
    String getLanguage(Document document);

    static void main(String[] args) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        String longStr = "其中包含一个URL www.baidu.com,一个带有端口号的URL http://www.jd.com:80, 一个带有路径的URL http://mallchat.cn, 还有美团技术文章https://mp.weixin.qq.com/s/hwTf4bDck9_tlFpgVDeIKg ";
//        String longStr = "一个带有端口号的URL http://www.jd.com:80,";
//        String longStr = "一个带有路径的URL http://mallchat.cn";
        PrioritizedUrlDiscover discover = new PrioritizedUrlDiscover();
        final Map<String, UrlInfoDTO> map = discover.getUrlContentMap(longStr);
        System.out.println(map);
        stopWatch.stop();
        long cost = stopWatch.getTotalTimeMillis();
        System.out.println(cost);
    }
}
