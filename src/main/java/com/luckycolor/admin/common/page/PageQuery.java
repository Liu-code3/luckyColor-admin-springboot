package com.luckycolor.admin.common.page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQuery {

    private static final long DEFAULT_PAGE_NO = 1L;

    private static final long DEFAULT_PAGE_SIZE = 20L;

    private static final long MAX_PAGE_SIZE = 200L;

    @Schema(description = "Page number, starts from 1", example = "1", defaultValue = "1")
    private Long pageNo = DEFAULT_PAGE_NO;

    @Schema(description = "Page size, max 200", example = "20", defaultValue = "20")
    private Long pageSize = DEFAULT_PAGE_SIZE;

    public long resolvePageNo() {
        if (pageNo == null || pageNo < 1) {
            return DEFAULT_PAGE_NO;
        }
        return pageNo;
    }

    public long resolvePageSize() {
        if (pageSize == null || pageSize < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
