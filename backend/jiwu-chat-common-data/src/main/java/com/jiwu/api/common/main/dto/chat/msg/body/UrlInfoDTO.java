package com.jiwu.api.common.main.dto.chat.msg.body;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UrlInfoDTO {
    /**
     * 标题
     **/
    String title;

    /**
     * 描述
     **/
    String description;

    /**
     * 网站LOGO/大图片
     **/
    String image;

    /**
     * 网站图标 (favicon)
     **/
    String icon;

    /**
     * 网站名称
     **/
    String siteName;

    /**
     * 网站URL
     **/
    String url;

    /**
     * 网站类型 (website, article, video等)
     **/
    String type;

    /**
     * 作者
     **/
    String author;

    /**
     * 发布者
     **/
    String publisher;

    /**
     * 语言
     **/
    String language;


}
