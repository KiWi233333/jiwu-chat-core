package com.jiwu.api.common.util.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 数组工具
 *
 * @className: ArrayUtils
 * @author: Kiwi23333
 * @description: 数组工具
 * @date: 2023/9/15 18:17
 */
public class ArrayUtil {

    public static Set<String> diffAddString(String[] oldArr, String[] newArr) {
        Set<String> set1 = new HashSet<>(Arrays.asList(oldArr));
        Set<String> set2 = new HashSet<>(Arrays.asList(newArr));

        Set<String> added = new HashSet<>(set2);
        added.removeAll(set1);
        return added;
    }

    public static Set<String> diffRemoveString(String[] oldArr, String[] newArr) {
        Set<String> set1 = new HashSet<>(Arrays.asList(oldArr));
        Set<String> set2 = new HashSet<>(Arrays.asList(newArr));

        Set<String> removed = new HashSet<>(set1);
        removed.removeAll(set2);
        return removed;
    }

}
