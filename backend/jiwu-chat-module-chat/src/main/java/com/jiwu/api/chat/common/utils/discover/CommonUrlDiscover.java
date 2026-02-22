package com.jiwu.api.chat.common.utils.discover;

import cn.hutool.core.text.CharSequenceUtil;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kiwi233
 */
public class CommonUrlDiscover extends AbstractUrlDiscover {

    // 预编译正则表达式
    private static final Pattern SENTENCE_PATTERN = Pattern.compile("[。！？.!?]");
    private static final Pattern BASE_URL_PATTERN = Pattern.compile("(https?://[^/]+)");
    private static final Pattern HTTP_PATTERN = Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE);

    // 核心选择器
    private static final String[] TITLE_SELECTORS = {
            "meta[property=og:title]", "meta[name=twitter:title]", "meta[name=title]"
    };

    private static final String[] DESCRIPTION_SELECTORS = {
            "meta[name=description]", "meta[property=og:description]", "meta[name=twitter:description]"
    };

    private static final String[] IMAGE_SELECTORS = {
            "meta[property=og:image]", "meta[name=twitter:image]", "meta[name=image]",
    };

    private static final String[] ICON_SELECTORS = {
            "link[rel=icon]", "link[rel=shortcut icon]", "link[rel=apple-touch-icon]"
    };

    private static final String[] SITE_NAME_SELECTORS = {
            "meta[property=og:site_name]", "meta[name=application-name]"
    };

    private static final String[] AUTHOR_SELECTORS = {
            "meta[name=author]", "meta[property=article:author]"
    };

    private static final String[] PUBLISHER_SELECTORS = {
            "meta[property=article:publisher]", "meta[name=publisher]"
    };

    // 常用关键词
    private static final String[] ICON_KEYWORDS = {"favicon", "logo", "icon", "brand"};
    
    // 常见图标路径
    private static final String[] COMMON_ICON_PATHS = {
            "/favicon.ico", "/favicon.png", "/apple-touch-icon.png", "/icon.png"
    };
    
    // 常量定义
    private static final int MAX_DESCRIPTION_LENGTH = 150;
    private static final String ELLIPSIS = "...";
    private static final String DEFAULT_FAVICON_PATH = "/favicon.ico";
    private static final String DEFAULT_TYPE = "website";

    @Nullable
    @Override
    public String getTitle(Document document) {
        if (document == null) return null;
        
        Element head = document.head();

        String title = findFirstValidContent(head, TITLE_SELECTORS);
        if (title != null) return title;

        title = document.title();
        return CharSequenceUtil.isNotBlank(title) ? title : null;
    }

    @Nullable
    @Override
    public String getDescription(Document document) {
        if (document == null) return null;
        
        Element head = document.head();

        String description = findFirstValidContent(head, DESCRIPTION_SELECTORS);
        return description != null ? processDescription(description) : null;
    }

    @Nullable
    @Override
    public String getImage(String url, Document document) {
        if (url == null || document == null) return null;
        Element head = document.head();
        // 优先meta标签
        String image = findFirstValidContent(head, IMAGE_SELECTORS);
        if (image != null) return buildFullUrl(url, image);

        // 其次img标签首图
        Elements imgs = document.select("img[src]");
        for (Element img : imgs) {
            String src = img.attr("src");
            if (CharSequenceUtil.isNotBlank(src)) {
                return buildFullUrl(url, src);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getIcon(String url, Document document) {
        if (url == null || document == null) return null;
        Element head = document.head();
        if (head != null) {
            String href = findFirstValidAttr(head, ICON_SELECTORS, "href");
            if (href != null) {
                return buildFullUrl(url, href);
            }
        }
        // fallback到默认路径
        return buildFullUrl(url, DEFAULT_FAVICON_PATH);
    }

    @Nullable
    @Override
    public String getSiteName(Document document) {
        if (document == null) return null;

        Element head = document.head();
        String siteName = findFirstValidContent(head, SITE_NAME_SELECTORS);
        if (siteName != null) return siteName;

        // 从标题提取网站名称
        String title = document.title();
        if (CharSequenceUtil.isNotBlank(title)) {
            String[] parts = title.split("[\\s\\-|]+");
            if (parts.length > 1) {
                return parts[parts.length - 1].trim();
            }
        }

        return null;
    }

    @Nullable
    @Override
    public String getType(Document document) {
        if (document == null) return DEFAULT_TYPE;
        
        Element head = document.head();

        String type = head.select("meta[property=og:type]").attr("content");
        return CharSequenceUtil.isNotBlank(type) ? type : DEFAULT_TYPE;
    }

    @Nullable
    @Override
    public String getAuthor(Document document) {
        if (document == null) return null;
        
        Element head = document.head();
        return findFirstValidContent(head, AUTHOR_SELECTORS);
    }

    @Nullable
    @Override
    public String getPublisher(Document document) {
        if (document == null) return null;
        
        Element head = document.head();
        return findFirstValidContent(head, PUBLISHER_SELECTORS);
    }

    @Nullable
    @Override
    public String getLanguage(Document document) {
        if (document == null) return null;
        
        // 检查html标签的lang属性
        Elements htmlElements = document.select("html");
        if (!htmlElements.isEmpty()) {
            String language = htmlElements.first().attr("lang");
            if (CharSequenceUtil.isNotBlank(language)) {
                return language;
            }
        }

        // 查找meta标签
        Element head = document.head();
        String[] metaSelectors = {"meta[http-equiv=content-language]", "meta[name=language]"};
        return findFirstValidContent(head, metaSelectors);

    }

    // 辅助方法
    
    /**
     * 统一的内容查找方法
     */
    private String findFirstValidContent(Element head, String[] selectors) {
        if (head == null || selectors == null) return null;
        
        for (String selector : selectors) {
            Elements elements = head.select(selector);
            if (!elements.isEmpty()) {
                String content = Objects.requireNonNull(elements.first()).attr("content");
                if (CharSequenceUtil.isNotBlank(content)) {
                    return content;
                }
            }
        }
        return null;
    }

    /**
     * 统一的属性查找方法
     */
    private String findFirstValidAttr(Element head, String[] selectors, String attrName) {
        if (head == null || selectors == null || attrName == null) return null;
        
        for (String selector : selectors) {
            Elements elements = head.select(selector);
            if (!elements.isEmpty()) {
                String attr = elements.first().attr(attrName);
                if (CharSequenceUtil.isNotEmpty(attr)) {
                    return attr;
                }
            }
        }
        return null;
    }

    /**
     * 检查字符串是否包含任一关键词
     */
    private boolean containsAnyIgnoreCase(String text, String[] keywords) {
        if (text == null || keywords == null) return false;
        
        String lowerText = text.toLowerCase();
        for (String keyword : keywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建完整URL
     */
    private String buildFullUrl(String baseUrl, String href) {
        if (CharSequenceUtil.isBlank(href)) return null;
        // 完整URL
        if (HTTP_PATTERN.matcher(href).find()) {
            return href;
        }
        // 协议相对
        if (href.startsWith("//")) {
            return baseUrl.startsWith("https://") ? "https:" + href : "http:" + href;
        }
        // 相对路径
        Matcher matcher = BASE_URL_PATTERN.matcher(baseUrl);
        if (!matcher.find()) return null;
        String baseUrlOnly = matcher.group(1);
        if (href.startsWith("/")) {
            return baseUrlOnly + href;
        } else {
            // 去掉baseUrlOnly末尾斜杠
            String base = baseUrlOnly.endsWith("/") ? baseUrlOnly.substring(0, baseUrlOnly.length() - 1) : baseUrlOnly;
            return base + "/" + href;
        }
    }

    /**
     * 处理描述内容
     */
    private String processDescription(String content) {
        if (CharSequenceUtil.isBlank(content)) return null;

        // 查找句子结束符
        Matcher matcher = SENTENCE_PATTERN.matcher(content);
        if (matcher.find()) {
            int endIndex = matcher.start();
            if (endIndex > 0) {
                return content.substring(0, endIndex);
            }
        }

        // 截取长度
        if (content.length() <= MAX_DESCRIPTION_LENGTH) {
            return content;
        }
        
        return content.substring(0, MAX_DESCRIPTION_LENGTH) + ELLIPSIS;
    }
}
