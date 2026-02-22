package com.jiwu.api.user.listener;

import com.jiwu.api.common.main.pojo.sys.Permission;
import com.jiwu.api.user.service.AdminUserPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

/**
 * 权限初始化监听器
 * 应用启动完成后自动初始化权限列表（可通过配置控制是否启用）
 *
 * @author Jiwu System
 * @date 2025-11-23
 */
@Slf4j
@Component
public class PermissionInitListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private AdminUserPermissionService adminUserPermissionService;

    /**
     * 是否自动初始化权限，从配置文件读取
     * 配置项: permission.auto-init.enabled
     * 默认值: false
     */
    @Value("${system.permission.auto-init.enabled:false}")
    private boolean autoInitEnabled;

    /**
     * 应用启动完成后自动初始化权限列表
     *
     * @param event 应用就绪事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!autoInitEnabled) {
            log.info("权限自动初始化已禁用（system.permission.auto-init.enabled=false），跳过权限初始化");
            return;
        }

        try {
            log.info("开始初始化权限列表...");
            List<Permission> newPermissions = adminUserPermissionService.initPermission();

            if (newPermissions != null && !newPermissions.isEmpty()) {
                log.info("权限初始化成功，新增 {} 个权限:", newPermissions.size());
            } else {
                log.info("权限初始化完成,没有新增权限");
            }
        } catch (Exception e) {
            log.error("权限初始化失败: {}", e.getMessage(), e);
        }
    }
}
