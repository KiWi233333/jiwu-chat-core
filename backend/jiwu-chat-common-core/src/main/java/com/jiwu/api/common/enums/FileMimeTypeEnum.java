package com.jiwu.api.common.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 */
@Getter
public enum FileMimeTypeEnum {
    TXT("text/plain", "txt"),
    EXCEL("application/vnd.ms-excel", "xls"),
    XLSX("application/vnd.-openxmlformats-officedocument.-spreadsheetml.-sheet", "xlsx"),
    PDF("application/pdf, application/x-pdf, application/x-bzpdf, application/x-gzpdf", "pdf"),
    PPT("application/vnd.ms-powerpoint", "ppt"),
    PPTX("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx"),
    DOC("application/msword", "doc"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"),
    ;
    // mimeType类型
    private final String value;
    // 文件后缀
    private final String suffix;


    FileMimeTypeEnum(String value, String suffix) {
        this.value = value;
        this.suffix = suffix;
    }

    /**
     * 校验文件类型
     *
     * @param mimeType 七牛云mimeType
     * @return FILE_MIME_TYPE
     */
    public static FileMimeTypeEnum checkType(String mimeType) {
        for (FileMimeTypeEnum type : FileMimeTypeEnum.values()) {
            if (mimeType.contains(type.value)) {
                return type;
            }
        }
        return null;
    }
}
