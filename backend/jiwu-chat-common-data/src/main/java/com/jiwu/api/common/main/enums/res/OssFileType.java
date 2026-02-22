package com.jiwu.api.common.main.enums.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum OssFileType {

    IMAGE("image", "image/", 0, 12000L, 1024L * 1024 * 10, "image/*"),

    VIDEO("video", "video/", 1, 12000L, 1024L * 1024 * 30, "video/*;"),// mp4. WebM、Ogg、Flv

    FILE("file", "file/", 2, 12000L, 1024L * 1024 * 100, "!image/*;!video/*"),// 2min

    FONT("font","font/", 3, 12000L, 1024L * 1024 * 10, "font/*"),

    AUDIO("audio", "audio/", 4, 12000L, 1024L * 1024 * 10, "audio/mp3;audio/x-mpeg;audio/mpeg;audio/webm;audio/wav;video/webm;"),
    ;
// video/mp4;video/webm;video/mpeg;video/x-flv;对应文件后缀 .mp4 .webm .mpeg .flv

    private final String type;
    private final String path;
    private final Integer code;
    private final Long timeOut;
    private final Long fileSize;
    private final String fileType;

    public static Map<String, OssFileTypeInfo> getTypeMap() {
        HashMap<String, OssFileTypeInfo> map = new HashMap<>();
        for (OssFileType fileType : OssFileType.values()) {
            map.put(fileType.getType(), OssFileTypeInfo.builder()
                    .type(fileType.getType())
                    .path(fileType.getPath())
                    .code(fileType.getCode())
                    .fileType(fileType.getFileType())
                    .timeOut(fileType.getTimeOut())
                    .fileSize(fileType.getFileSize())
                    .build());
        }
        return map;
    }

    @Data
    @Builder
    public static class OssFileTypeInfo {
        private String type;
        private String path;
        private Integer code;
        private Long timeOut;
        private Long fileSize;
        private String fileType;

    }
}
