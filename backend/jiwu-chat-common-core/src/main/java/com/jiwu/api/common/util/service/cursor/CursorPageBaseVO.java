package com.jiwu.api.common.util.service.cursor;

import cn.hutool.core.collection.CollUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 游标翻页返回
 */
@Data
@Schema(description = "游标翻页返回")
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageBaseVO<T> {

    @Schema(description = "游标（下次翻页带上这参数）")
    private String cursor;

    @Schema(description = "是否最后一页")
    private Boolean isLast = Boolean.FALSE;

    @Schema(description = "数据列表")
    private List<T> list;

    public static <T> CursorPageBaseVO<T> init(CursorPageBaseVO<?> cursorPage, List<T> list) {
        CursorPageBaseVO<T> cursorPageBaseVO = new CursorPageBaseVO<>();
        cursorPageBaseVO.setIsLast(cursorPage.getIsLast());
        cursorPageBaseVO.setList(list);
        cursorPageBaseVO.setCursor(cursorPage.getCursor());
        return cursorPageBaseVO;
    }

    @JsonIgnore
    public Boolean isEmpty() {
        return CollUtil.isEmpty(list);
    }

    public static <T> CursorPageBaseVO<T> empty() {
        CursorPageBaseVO<T> cursorPageBaseVO = new CursorPageBaseVO<>();
        cursorPageBaseVO.setIsLast(true);
        cursorPageBaseVO.setList(new ArrayList<T>());
        return cursorPageBaseVO;
    }

}
