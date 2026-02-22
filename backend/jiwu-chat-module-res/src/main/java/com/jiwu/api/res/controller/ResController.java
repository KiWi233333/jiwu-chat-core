package com.jiwu.api.res.controller;

import cn.hutool.core.util.URLUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.jiwu.api.common.annotation.IgnoreAuth;
import com.jiwu.api.common.annotation.PortFlowControl;
import com.jiwu.api.common.annotation.ReqPermission;
import com.jiwu.api.common.config.oss.FileOSSConfig;
import com.jiwu.api.common.config.qrcode.QrCodeService;
import com.jiwu.api.common.enums.ResultStatus;
import com.jiwu.api.common.main.dto.chat.cursor.MsgContentConstant;
import com.jiwu.api.common.util.common.AssertUtil;
import com.jiwu.api.common.util.common.DomainUtil;
import com.jiwu.api.common.util.service.RequestHolderUtil;
import com.jiwu.api.common.util.service.Result;
import com.jiwu.api.res.common.dto.QRCodeOptionDTO;
import com.jiwu.api.common.main.enums.res.OssFileType;
import com.jiwu.api.common.main.vo.res.FileOssVO;
import com.jiwu.api.common.main.vo.res.SystemConstantVO;
import com.jiwu.api.res.service.ResService;
import com.jiwu.api.common.constant.JwtConstant;
import com.jiwu.api.common.constant.UserConstant;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import net.dreamlu.mica.ip2region.core.IpInfo;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 资源模块
 */
@Slf4j
@Tag(name = "资源模块")
@RestController
@RequestMapping("/res")
public class ResController {

    @Resource
    private FileOSSConfig fileOSSConfig;
    @Resource
    private ResService resService;
    @Resource
    private QrCodeService qrCodeService;
    private final String TIME_FORMAT_YEAR = "yyyy-MM-dd";

    @Operation(summary = "获取客户端常量信息", tags = {"资源模块"})
    @GetMapping("/oss/constant")
    @IgnoreAuth
    Result<Map<String, OssFileType.OssFileTypeInfo>> getOssConstant() {
        return Result.ok(OssFileType.getTypeMap());
    }

    @Operation(summary = "获取系统常量信息", tags = {"资源模块"})
    @GetMapping("/system/constant")
    @IgnoreAuth
    Result<SystemConstantVO> getSystemConstant() {
        return Result.ok(SystemConstantVO.builder()
                .ossInfo(OssFileType.getTypeMap())
                .msgInfo(MsgContentConstant.getMsgConstantInfoMap())
                .build());
    }

    @Operation(summary = "获取上传临时凭证（图片）", tags = {"资源模块"})
    @GetMapping("/user/image")
    @ReqPermission(name = "获取上传临时凭证（图片）", intro = "用户消费者获取上传临时凭证（图片）", expression = "res:user:image:add")
    @PortFlowControl(limit = 10, time = 5, timeUnit = TimeUnit.MINUTES, errorMessage = "上传图片数量过多，请稍后再试！（10张/5分钟）")
    Result<FileOssVO> getUploadImageToken(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        // 获取用户id
        String userId = RequestHolderUtil.get().getId();
        // 获取当前时间
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_YEAR));
        return resService.getUploadToken(OssFileType.IMAGE, formattedTime + "/", UUID.randomUUID().toString(), userId);
    }

    @Operation(summary = "获取上传临时凭证（音频）", tags = {"资源模块"})
    @GetMapping("/user/sound")
    @ReqPermission(name = "获取上传临时凭证（音频）", intro = "用户消费者获取上传临时凭证（音频）", expression = "res:user:image:audio")
    @PortFlowControl(limit = 10, time = 3, timeUnit = TimeUnit.MINUTES, errorMessage = "上传音频数量过多，请稍后再试！（10条/3分钟）")
    Result<FileOssVO> getUploadAudioToken(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        // 获取用户id
        String userId = RequestHolderUtil.get().getId();
        // 获取当前时间
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_YEAR));
        return resService.getUploadToken(OssFileType.AUDIO, formattedTime + "/", UUID.randomUUID().toString(), userId);
    }

    @Operation(summary = "获取上传临时凭证（视频）", tags = {"资源模块"})
    @GetMapping("/user/video")
    @ReqPermission(name = "获取上传临时凭证（视频）", intro = "用户消费者获取上传临时凭证（视频）", expression = "res:user:video:add")
    @PortFlowControl(limit = 5, time = 30, timeUnit = TimeUnit.MINUTES, errorMessage = "上传视频数量过多，请稍后再试！（5条/30分钟）")
    Result<FileOssVO> getUploadVideoToken(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        // 获取用户id
        String userId = RequestHolderUtil.get().getId();
        // 获取当前时间
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_YEAR));
        return resService.getUploadToken(OssFileType.VIDEO, formattedTime + "/", UUID.randomUUID().toString(), userId);
    }


    @Operation(summary = "获取上传临时凭证（文件）", tags = {"资源模块"})
    @GetMapping("/user/file")
    @ReqPermission(name = "获取上传临时凭证（文件）", intro = "用户消费者获取上传临时凭证（文件）", expression = "res:user:file:add")
    @PortFlowControl(limit = 6, time = 1, timeUnit = TimeUnit.HOURS, errorMessage = "上传文件数量过多，请稍后再试！（6个/1小时）")
    Result<FileOssVO> getUploadFileToken(@RequestHeader(name = JwtConstant.HEADER_NAME) String token) {
        // 获取用户id
        String userId = RequestHolderUtil.get().getId();
        // 获取当前时间
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern(TIME_FORMAT_YEAR));
        return resService.getUploadToken(OssFileType.FILE, formattedTime + "/", UUID.randomUUID().toString(), userId);
    }


    @Operation(summary = "删除oss未使用文件", tags = {"资源模块"})
    @ReqPermission(name = "删除oss未使用文件", intro = "用户消费者删除oss未使用文件", expression = "res:user:files:del")
    @DeleteMapping("/user/files")
    @PortFlowControl(limit = 5, time = 5, timeUnit = TimeUnit.MINUTES)
    Result<Object> toDeleteOssFile(@RequestHeader(name = JwtConstant.HEADER_NAME) String token, HttpServletRequest request, @RequestParam() String key) {
        if (StringUtils.isBlank(key)) {
            return Result.fail(ResultStatus.NULL_ERR.getCode(), "参数不能为空！");
        }
        // 获取用户id
        String userId = request.getAttribute(UserConstant.USER_ID_KEY).toString();
        return resService.deleteOssFile(key, userId, true);
    }


    @Resource
    private Ip2regionSearcher ip2regionSearcher;

    @Operation(summary = "获取IP解析信息", tags = {"资源模块"})
    @GetMapping("/ip/info")
    @PortFlowControl(limit = 60, time = 5, timeUnit = TimeUnit.SECONDS)
    Result<IpInfo> getIpInfo(@RequestHeader(name = JwtConstant.HEADER_NAME) String token,
                             HttpServletRequest request,
                             @RequestParam() String ip) {
        if (StringUtils.isBlank(ip)) {
            return Result.fail(ResultStatus.NULL_ERR.getCode(), "参数不能为空！");
        }
        return Result.ok(ip2regionSearcher.memorySearch(ip));
    }


    @Operation(summary = "转化二维码", tags = {"资源模块"})
    @GetMapping("/qrcode/stream")
    @PortFlowControl(limit = 60, time = 15, errorMessage = "同时间请求过多，请稍后再试！")
    Result<Object> getQrcodeByUrl(
            String content,
            QRCodeOptionDTO dto,
            HttpServletResponse servletResponse) {
        // 校验是否没有content
        AssertUtil.isNotBlank(content, "二维码内容不能为空！");
        String normalUrl = DomainUtil.isValidDomain(content) ? URLUtil.normalize(content) : content;
        if (!qrCodeService.createCodeToStream(normalUrl, servletResponse, QRCodeOptionDTO.mergeConfig(qrCodeService.getQrConfig(), dto))) {
            return Result.fail("内容转二维码错误，请重试！");
        }
        return Result.ok("操作成功！");
    }


    @Operation(summary = "OSS资源", tags = {"资源模块"})
    @GetMapping("/oss/**")
    void getOssRes(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String originUrl = fileOSSConfig.hostName + request.getRequestURI().split("/res/oss")[1];
            response.sendRedirect(originUrl);
        } catch (Exception e) {
            response.sendError(404);
        }
    }

    @Operation(summary = "OSS资源", tags = {"资源模块"})
    @GetMapping("/image/**")
    void getImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String originUrl = fileOSSConfig.hostName + request.getRequestURI().split("/res")[1];
            response.sendRedirect(originUrl);
        } catch (Exception e) {
            response.sendError(404);
        }
    }

    @Operation(summary = "视频资源", tags = {"资源模块"})
    @GetMapping("/video/**")
    void getVideo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String originUrl = fileOSSConfig.hostName + request.getRequestURI().split("/res")[1];
            response.sendRedirect(originUrl);
        } catch (Exception e) {
            response.sendError(404);
        }
    }

    @Operation(summary = "文件资源", tags = {"资源模块/工具模块"})
    @GetMapping("/file/**")
    void getFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String originUrl = fileOSSConfig.hostName + request.getRequestURI().split("/res")[1];
            response.sendRedirect(originUrl);
        } catch (Exception e) {
            response.sendError(404);
        }
    }

}
