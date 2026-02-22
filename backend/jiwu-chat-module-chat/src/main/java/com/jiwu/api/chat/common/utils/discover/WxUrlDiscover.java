package com.jiwu.api.chat.common.utils.discover;

import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;

/**
 * 针对微信公众号文章的标题获取类
 * @author kiwi233
 */
public class WxUrlDiscover extends AbstractUrlDiscover {

    @Nullable
    @Override
    public String getTitle(Document document) {
        return document.getElementsByAttributeValue("property", "og:title").attr("content");
    }

    @Nullable
    @Override
    public String getDescription(Document document) {
        return document.getElementsByAttributeValue("property", "og:description").attr("content");
    }

    @Nullable
    @Override
    public String getImage(String url, Document document) {
        String href = document.getElementsByAttributeValue("property", "og:image").attr("content");
        return (href.isEmpty() || !isConnect(href)) ? null : href;
    }

    @Nullable
    @Override
    public String getIcon(String url, Document document) {
        // 微信公众号通常没有专门的图标，使用默认favicon
        return buildFullUrl(url, "/favicon.ico");
    }

    private String buildFullUrl(String baseUrl, String href) {
        if (href.startsWith("http")) {
            return href;
        }
        return baseUrl.replaceAll("(https?://[^/]+).*", "$1") + 
               (href.startsWith("/") ? href : "/" + href);
    }

    @Nullable
    @Override
    public String getSiteName(Document document) {
        return document.getElementsByAttributeValue("property", "og:site_name").attr("content");
    }

    @Nullable
    @Override
    public String getType(Document document) {
        return document.getElementsByAttributeValue("property", "og:type").attr("content");
    }

    @Nullable
    @Override
    public String getAuthor(Document document) {
        // 微信公众号文章作者通常在 og:article:author 中
        String author = document.getElementsByAttributeValue("property", "og:article:author").attr("content");
        if (author.isEmpty()) {
            author = document.getElementsByAttributeValue("property", "article:author").attr("content");
        }
        return author.isEmpty() ? null : author;
    }

    @Nullable
    @Override
    public String getPublisher(Document document) {
        // 微信公众号名称通常在 og:site_name 中
        String publisher = document.getElementsByAttributeValue("property", "og:site_name").attr("content");
        if (publisher.isEmpty()) {
            publisher = document.getElementsByAttributeValue("property", "article:publisher").attr("content");
        }
        return publisher.isEmpty() ? null : publisher;
    }

    @Nullable
    @Override
    public String getLanguage(Document document) {
        return document.select("html").attr("lang");
    }


}
