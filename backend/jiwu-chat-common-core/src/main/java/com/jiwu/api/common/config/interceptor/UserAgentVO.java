package com.jiwu.api.common.config.interceptor;

import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.OperatingSystem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import net.dreamlu.mica.ip2region.core.IpInfo;

import java.io.Serializable;

/**
 * 描述
 *
 * @className: UserAgentVO
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/7/25 19:59
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class UserAgentVO implements Serializable {
    private static final long serialVersionUID = 7025462762784240222L;
    @Schema(description = "id")
    private int id;
    @Schema(description = "是否本机")
    private int isLocal;
    @Schema(description = "ip地址")
    private String ip;
    @Schema(description = "操作系统")
    private OperatingSystem operatingSystem;
    @Schema(description = "浏览器")
    private Browser browser;
    @Schema(description = "字符")
    private String userAgentString;
    @Schema(description = "IP地址信息")
    private IpInfo ipInfo;


    public static UserAgentVO parseUserAgents(String uaStr, String ip) {
        String userAgentLowercaseString = uaStr == null ? null : uaStr.toLowerCase();
        Browser browser = Browser.parseUserAgentLowercaseString(userAgentLowercaseString);
        OperatingSystem operatingSystem = OperatingSystem.UNKNOWN;
        if (browser != Browser.BOT) {
            operatingSystem = OperatingSystem.parseUserAgentLowercaseString(userAgentLowercaseString);
        }
        return new UserAgentVO()
                .setOperatingSystem(operatingSystem)
                .setBrowser(browser)
                .setId((operatingSystem.getId() << 16) + browser.getId())
                .setUserAgentString(uaStr)
                .setIp(ip);
    }
}
