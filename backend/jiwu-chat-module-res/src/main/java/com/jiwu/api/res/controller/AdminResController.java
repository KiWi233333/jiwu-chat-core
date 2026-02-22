package com.jiwu.api.res.controller;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.common.util.service.OSS.OssFileUtil;
import com.jiwu.api.common.main.enums.res.OssFileType;
import com.jiwu.api.common.main.vo.res.FileOssVO;
import com.jiwu.api.res.service.ResService;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Tag(name = "资源模块", description = "管理员模块")
@RestController
@RequestMapping("/admin/res")
public class AdminResController {

    private static final String DATE_FORMAT_TEXT = "yyyy-MM-dd";
    @Autowired
    ResService resService;
    @Autowired
    OssFileUtil ossFileUtil;

    @GetMapping("/image")
    @Parameter(name = JwtConstant.HEADER_NAME, description = "用户token", required = true, in = ParameterIn.HEADER)
    @Operation(summary = "获取上传临时凭证（图片）", tags = {"资源模块"})
    @ReqPermission(name = "获取上传临时凭证（图片）(管理员)", intro = "获取上传临时凭证（图片）(管理员)", expression = "admin:res:image:add")
    @PortFlowControl(limit = 10, time = 10)
        // 节流
    Result<FileOssVO> getUploadImageToken(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request) {
        // 获取用户id
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        // 获取当前时间
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_TEXT));
        return resService.getUploadToken(OssFileType.IMAGE, formattedTime + "/", UUID.randomUUID().toString(), userId);
    }

    @Operation(summary = "获取上传临时凭证（视频）", tags = {"资源模块"})
    @ReqPermission(name = "获取上传临时凭证（视频）(管理员)", intro = "获取上传临时凭证（视频）(管理员)", expression = "admin:res:video:add")
    @Parameter(name = JwtConstant.HEADER_NAME, description = "token", required = true, in = ParameterIn.HEADER)
    @GetMapping("/video")
    @PortFlowControl(limit = 10, time = 10)
    Result<FileOssVO> getUploadVideoToken(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request) {
        // 获取用户id
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        // 获取当前时间
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_TEXT));
        return resService.getUploadToken(OssFileType.VIDEO, formattedTime + "/", UUID.randomUUID().toString(), userId);
    }


    @Operation(summary = "获取上传临时凭证（文件）", tags = {"资源模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "token", required = true, in = ParameterIn.HEADER)
    @ReqPermission(name = "获取上传临时凭证（文件）(管理员)", intro = "获取上传临时凭证（文件）(管理员)", expression = "admin:res:file:add")
    @GetMapping("/file")
    @PortFlowControl(limit = 10, time = 10)
    Result<FileOssVO> getUploadFileToken(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request) {
        // 获取用户id
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        // 获取当前时间
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_TEXT));
        return resService.getUploadToken(OssFileType.FILE, formattedTime + "/", UUID.randomUUID().toString(), userId);
    }


    @Operation(summary = "删除oss未使用文件", tags = {"资源模块"})
    @Parameter(name = JwtConstant.HEADER_NAME, description = "token", required = true, in = ParameterIn.HEADER)
    @ReqPermission(name = "删除oss未使用文件(管理员)", intro = "删除oss未使用文件(管理员)", expression = "admin:res:file:del")
    @DeleteMapping("/file")
    @PortFlowControl(limit = 10, time = 10)
    Result<Object> toDeleteOssFile(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request, @RequestParam() String key) {
        if (StringUtils.isBlank(key)) {
            return Result.fail(ResultStatus.NULL_ERR.getCode(), "参数不能为空！");
        }
        // 获取用户id
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return resService.deleteOssFile(key, userId, false);
    }

    @DeleteMapping("/expense")
    @Operation(summary = "消费Oss文件", tags = {"资源模块"})
    @ReqPermission(name = "消费Oss文件（管理员）", intro = "消费Oss文件（管理员），谨慎使用", expression = "admin:res:file:edit")
    @Parameter(name = JwtConstant.HEADER_NAME, description = "token", required = true, in = ParameterIn.HEADER)
    @PortFlowControl(limit = 10, time = 10)
    Result<Object> toDoneOssFile(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request, @RequestParam() String key) {
        if (StringUtils.isBlank(key)) {
            return Result.fail(ResultStatus.NULL_ERR.getCode(), "参数不能为空！");
        }
        // 获取用户id
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        if (ossFileUtil.deleteRedisKey(userId, key)) {
            return Result.ok(1);
        } else {
            return Result.fail(ResultStatus.DELETE_ERR.getCode(), "抱歉，该文件没有访问权限！");
        }
    }



}
