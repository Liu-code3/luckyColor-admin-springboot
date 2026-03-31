package com.luckycolor.admin.infrastructure.persistence.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckycolor.admin.common.page.PageQuery;

public final class MyBatisUtils {

    private MyBatisUtils() {
    }

    public static <T> Page<T> buildPage(PageQuery pageQuery) {
        PageQuery safePageQuery = pageQuery == null ? new PageQuery() : pageQuery;
        return new Page<>(safePageQuery.resolvePageNo(), safePageQuery.resolvePageSize());
    }
}
