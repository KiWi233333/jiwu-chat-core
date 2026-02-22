package com.jiwu.api.user.service.impl;

import com.jiwu.api.common.config.interceptor.UserAgentVO;
import com.jiwu.api.common.util.service.IPUtil;
import com.jiwu.api.common.util.service.RedisUtil;
import com.jiwu.api.common.exception.BusinessException;
import com.jiwu.api.user.common.dto.DeleteSafeDTO;
import com.jiwu.api.common.constant.UserConstant;
import com.jiwu.api.user.service.UserSafeService;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 账户与安全实现
 *
 * @className: UserSafeServiceImpl
 * @author: Kiwi23333
 * @description: 账户与安全实现
 * @date: 2023/7/26 2:04
 */
@Service
public class UserSafeServiceImpl implements UserSafeService {
    @Resource
    private RedisUtil<String, String> redisUtil;

    @Resource(name = "ip2regionSearcher")
    private Ip2regionSearcher ip2regionSearcher;


    /**
     * 获取登录设备
     *
     * @param userId 用户id
     * @return List<UserAgentVO>
     */
    @Override
    public List<UserAgentVO> getUserLoginDevice(String userId, HttpServletRequest request) {
        // 1、获取登录设备
        Map<String, String> map = redisUtil.hGetAll(UserConstant.USER_REFRESH_TOKEN_KEY + userId);
        String localIp = IPUtil.getIpAddress(request);// 本机ip
        String userAgent = request.getHeader(UserConstant.USER_AGENT_KEY);// UserAgent
        // 2、有序化
        List<UserAgentVO> userAgentList = new ArrayList<>();
        for (String key : map.keySet()) {
            final String ip = String.valueOf(map.get(key));
            userAgentList.add(UserAgentVO.parseUserAgents(key, ip)
                    .setIsLocal(map.get(key).equals(localIp) && key.equals(userAgent) ? 1 : 0)
                    .setIpInfo(ip2regionSearcher.memorySearch(ip)));
        }
        // 3、返回数据
        return userAgentList;
    }

    /**
     * 用户下线指定用户
     *
     * @param userId 用户id
     * @param dto    dto
     * @return 下线设备数量
     */
    @Override
    public Integer toUserOfflineByOne(String userId, DeleteSafeDTO dto) {
        int count = 0;
        // 1、计算总数
        for (String ua : dto.getUserAgent()) {
            if (redisUtil.hExists(UserConstant.USER_REFRESH_TOKEN_KEY + userId, ua)) {
                count++;
            }
        }
        // 2、删除
        if (count == dto.getUserAgent().size()) {
            for (String ua : dto.getUserAgent()) {
                redisUtil.hDelete(UserConstant.USER_REFRESH_TOKEN_KEY + userId, ua);
            }
        } else {
            throw new BusinessException("用户已下线！");
        }
        // 3、返回下线数量
        return count;
    }


}
