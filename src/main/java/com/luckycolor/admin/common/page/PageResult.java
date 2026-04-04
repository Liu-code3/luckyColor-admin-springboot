package com.luckycolor.admin.common.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Generic paged result")
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResult<T> {

    @Schema(description = "Current page records")
    private final List<T> list;

    @Schema(description = "Total record count", example = "125")
    private final long total;

    public static <T> PageResult<T> of(List<T> list, long total) {
        List<T> safeList = list == null ? Collections.emptyList() : list;
        return new PageResult<>(safeList, total);
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return of(page.getRecords(), page.getTotal());
    }
}
