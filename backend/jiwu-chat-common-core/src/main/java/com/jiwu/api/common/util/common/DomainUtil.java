package com.jiwu.api.common.util.common;

import java.net.IDN;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Url工具类
 * Description: 判断是否为域名
 * Date: 2023-04-22
 */
public class DomainUtil {


    // 正则表达式，用于匹配域名的基本格式
    private static final String DOMAIN_NAME_PATTERN = "^(http(s)?://)?(?=.{1,255}$)[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*\\.[a-zA-Z]{2,63}$";

    // 有效的顶级域名（TLD）列表
    private static final Set<String> VALID_TOP_LEVEL_DOMAINS = new HashSet<>();

    static {
        // 初始化有效的TLD列表
        String[] tlds = {
                "com", "org", "net", "edu", "gov", "mil", "io", "co", "tech", "info", "biz", "asia", "mobi", "tv", "me", "aero",
                "name", "museum", "coop", "a[cdefgilmnoqrstuwxz]",
                "b[abdefghijmnorstvwyz]",
                "c[acdfghiklmnoruvxyz]",
                "d[ejkmoz]",
                "e[cegrstu]",
                "f[ijkmor]",
                "g[abdefghilmnpqrstuwy]",
                "h[kmnrtu]",
                "i[delmnoqrst]",
                "j[emop]",
                "k[eghimnprwyz]",
                "l[abcikrstuvy]",
                "m[acdeghklmnopqrstuvwxyz]",
                "n[acefgilopruz]",
                "om",
                "p[aefghklmnrstwy]",
                "qa",
                "r[eosuw]",
                "s[abcdegijklmnortuvyz]",
                "t[cdfghjklmnortvwz]",
                "u[agksyz]",
                "v[aceginu]",
                "w[fs]",
                "y[etu]",
                "z[a-m]",
                "biz", "uk", "de", "jp", "fr", "au", "us", "ru", "cn", "it", "kr", "in", "ca", "br", "mx"
        };
        VALID_TOP_LEVEL_DOMAINS.addAll(Arrays.asList(tlds));
    }

    /**
     * 验证域名是否有效。
     *
     * @param domain 要验证的域名
     * @return 如果域名有效，返回true；否则返回false。
     */
    public static boolean isValidDomain(String domain) {
        // 检查域名是否为空
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        // 将Punycode域名转换为Unicode
        try {
            domain = IDN.toUnicode(domain);
        } catch (IllegalArgumentException e) {
            return false;
        }

        // 使用正则表达式检查域名格式
        return !Pattern.matches(DOMAIN_NAME_PATTERN, domain);

        // 提取域名的TLD部分
        // String tld = domain.substring(domain.lastIndexOf('.') + 1);

        // 检查 TLD 是否在有效
        // return VALID_TOP_LEVEL_DOMAINS.stream().anyMatch(tld::matches);
    }


}
