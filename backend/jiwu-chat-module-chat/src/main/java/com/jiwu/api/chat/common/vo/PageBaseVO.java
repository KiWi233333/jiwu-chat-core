package com.jiwu.api.chat.common.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2023-03-19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "基础翻页返回数据")
public class PageBaseVO<T> {

    @Schema(description = "当前页数")
    private Integer current;

    @Schema(description = "每页查询数量")
    private Integer pageSize;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "是否最后一页")
    private Boolean isLast = Boolean.FALSE;

    @Schema(description = "数据列表")
    private List<T> records;


    public static <T> PageBaseVO<T> empty() {
        PageBaseVO<T> r = new PageBaseVO<>();
        r.setCurrent(1);
        r.setPageSize(0);
        r.setIsLast(true);
        r.setTotal(0L);
        r.setRecords(new ArrayList<>());
        return r;
    }

    public static <T> PageBaseVO<T> init(Integer pageNo, Integer pageSize, Long total, Boolean isLast, List<T> list) {
        return new PageBaseVO<>(pageNo, pageSize, total, isLast, list);
    }

    public static <T> PageBaseVO<T> init(Integer pageNo, Integer pageSize, Long total, List<T> list) {
        return new PageBaseVO<>(pageNo, pageSize, total, isLastPage(total, pageNo, pageSize), list);
    }

    public static <T> PageBaseVO<T> init(IPage<T> page) {
        return init((int) page.getCurrent(), (int) page.getSize(), page.getTotal(), page.getRecords());
    }

    public static <T> PageBaseVO<T> init(IPage page, List<T> list) {
        return init((int) page.getCurrent(), (int) page.getSize(), page.getTotal(), list);
    }

    public static <T> PageBaseVO<T> init(PageBaseVO resp, List<T> list) {
        return init(resp.getCurrent(), resp.getPageSize(), resp.getTotal(), resp.getIsLast(), list);
    }

    /**
     * 是否是最后一页
     */
    public static Boolean isLastPage(long total, int pageNo, int pageSize) {
        if (pageSize == 0) {
            return false;
        }
        long pageTotal = total / pageSize + (total % pageSize == 0 ? 0 : 1);
        return pageNo >= pageTotal;
    }
}
