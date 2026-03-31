package com.luckycolor.admin.common.page;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQuery {

    private static final long DEFAULT_PAGE_NO = 1L;

    private static final long DEFAULT_PAGE_SIZE = 20L;

    private static final long MAX_PAGE_SIZE = 200L;

    private Long pageNo = DEFAULT_PAGE_NO;

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
