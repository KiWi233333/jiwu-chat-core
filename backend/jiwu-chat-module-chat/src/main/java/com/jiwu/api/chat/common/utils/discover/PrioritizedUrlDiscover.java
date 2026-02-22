package com.jiwu.api.chat.common.utils.discover;

import cn.hutool.core.text.CharSequenceUtil;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * 具有优先级的title查询器
 * @author kiwi233
 */
public class PrioritizedUrlDiscover extends AbstractUrlDiscover {

    private final List<UrlDiscover> urlDiscovers = new ArrayList<>(2);

    public PrioritizedUrlDiscover() {
        urlDiscovers.add(new WxUrlDiscover());
        urlDiscovers.add(new CommonUrlDiscover());
    }


    @Nullable
    @Override
    public String getTitle(Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String urlTitle = urlDiscover.getTitle(document);
            if (CharSequenceUtil.isNotBlank(urlTitle)) {
                return urlTitle;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getDescription(Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String urlDescription = urlDiscover.getDescription(document);
            if (CharSequenceUtil.isNotBlank(urlDescription)) {
                return urlDescription;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getImage(String url, Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String urlImage = urlDiscover.getImage(url, document);
            if (CharSequenceUtil.isNotBlank(urlImage)) {
                return urlImage;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getIcon(String url, Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String urlIcon = urlDiscover.getIcon(url, document);
            if (CharSequenceUtil.isNotBlank(urlIcon)) {
                return urlIcon;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getSiteName(Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String siteName = urlDiscover.getSiteName(document);
            if (CharSequenceUtil.isNotBlank(siteName)) {
                return siteName;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String type = urlDiscover.getType(document);
            if (CharSequenceUtil.isNotBlank(type)) {
                return type;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getAuthor(Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String author = urlDiscover.getAuthor(document);
            if (CharSequenceUtil.isNotBlank(author)) {
                return author;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getPublisher(Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String publisher = urlDiscover.getPublisher(document);
            if (CharSequenceUtil.isNotBlank(publisher)) {
                return publisher;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String getLanguage(Document document) {
        for (UrlDiscover urlDiscover : urlDiscovers) {
            String language = urlDiscover.getLanguage(document);
            if (CharSequenceUtil.isNotBlank(language)) {
                return language;
            }
        }
        return null;
    }

}
