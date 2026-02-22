package com.jiwu.api.common.util.service;

/**
 * 文件工具类
 *
 * @className: FileUtil
 * @author: Kiwi23333
 * @description: TODO描述
 * @date: 2023/5/8 3:00
 */
public class FileUtil {

    // 图片允许的后缀扩展名
    public static String[] IMAGE_FILE_EXP = new String[]{"png", "webp", "bmp", "jpg", "jpeg", "gif", "svg"};

    /**
     * 判断是否为图片
     */
    public static boolean isImage(String fileName) {
        fileName = fileName.substring(fileName.lastIndexOf(".") + 1);
        for (String type : IMAGE_FILE_EXP) {
            if (type.equalsIgnoreCase(fileName)) {
                return true;
            }
        }
        return false;
    }

 }
