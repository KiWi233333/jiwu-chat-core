package com.jiwu.api.chat.common.utils.discover;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ReUtil;
import com.jiwu.api.common.main.dto.chat.msg.body.UrlInfoDTO;
import com.jiwu.api.chat.common.utils.MtFutureUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.util.Pair;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author kiwi233
 */
@Slf4j
public abstract class AbstractUrlDiscover implements UrlDiscover {
    //链接识别的正则
//    private static final Pattern PATTERN = Pattern.compile("(?:https?://)?(?:www\\.)?([a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)+)([/?#][^\\s]*)?");
    private static final Pattern PATTERN = Pattern.compile(
            "(?:https?://)?(?:www\\.)?" +                       // 可选 http(s):// 和 www.
                    "([a-zA-Z0-9-]{2,}\\.[a-zA-Z0-9-]{2,}(?:\\.[a-zA-Z0-9-]{2,})*)" + // 至少两个级别、每级不少于2个字符
                    "([/?#][^\\s]*)?"                                    // 可选路径部分
    );

    @Nullable
    @Override
    public Map<String, UrlInfoDTO> getUrlContentMap(String content) {
        if (CharSequenceUtil.isBlank(content)) {
            return new HashMap<>();
        }
        List<String> matchList = ReUtil.findAll(PATTERN, content, 0);

        //并行请求
        List<CompletableFuture<Pair<String, UrlInfoDTO>>> futures = matchList.stream().map(match -> CompletableFuture.supplyAsync(() -> {
            UrlInfoDTO urlInfo = getContent(match);
            return Objects.isNull(urlInfo) ? null : Pair.of(match, urlInfo);
        })).collect(Collectors.toList());
        CompletableFuture<List<Pair<String, UrlInfoDTO>>> future = MtFutureUtils.sequenceNonNull(futures);
        //结果组装
        return future.join().stream().collect(Collectors.toMap(Pair::getFirst, Pair::getSecond, (a, b) -> a));
    }

    @Nullable
    @Override
    public UrlInfoDTO getContent(String url) {
        Document document = getUrlDocument(assemble(url));
        if (Objects.isNull(document)) {
            return null;
        }

        final String image = getImage(assemble(url), document);
        final String icon = getIcon(assemble(url), document);
        return UrlInfoDTO.builder()
                .title(getTitle(document))
                .description(getDescription(document))
                .image(image != null ? image : icon)
                .icon(icon)
                .siteName(getSiteName(document))
                .url(assemble(url))
                .type(getType(document))
                .author(getAuthor(document))
                .publisher(getPublisher(document))
                .language(getLanguage(document))
                .build();
    }


    private String assemble(String url) {

        if (!CharSequenceUtil.startWith(url, "http")) {
            return "http://" + url;
        }

        return url;
    }

    protected Document getUrlDocument(String matchUrl) {
        try {
            Connection connect = Jsoup.connect(matchUrl);
            connect.timeout(2000);
            return connect.get();
        } catch (Exception e) {
            log.error("find error:url:{}", matchUrl, e);
        }
        return null;
    }

    /**
     * 判断链接是否有效
     * 输入链接
     * 返回true或者false
     */
    public static boolean isConnect(String href) {
        //请求地址
        URL url;
        //请求状态码
        int state;
        //下载链接类型
        String fileType;
        try {
            url = new URL(href);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            state = httpURLConnection.getResponseCode();
            fileType = httpURLConnection.getHeaderField("Content-Disposition");
            //如果成功200，缓存304，移动302都算有效链接，并且不是下载链接
            if ((state == 200 || state == 302 || state == 304) && fileType == null) {
                return true;
            }
            httpURLConnection.disconnect();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}
