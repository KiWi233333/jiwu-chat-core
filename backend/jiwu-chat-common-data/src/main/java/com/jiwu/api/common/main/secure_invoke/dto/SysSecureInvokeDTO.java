package com.jiwu.api.common.main.secure_invoke.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统安全调用DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysSecureInvokeDTO {
    private String className;
    private String methodName;
    private String parameterTypes;
    private String args;
}
