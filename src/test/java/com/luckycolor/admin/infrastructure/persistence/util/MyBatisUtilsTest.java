package com.luckycolor.admin.infrastructure.persistence.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luckycolor.admin.common.page.PageQuery;
import org.junit.jupiter.api.Test;

class MyBatisUtilsTest {

    @Test
    void shouldBuildPageFromQuery() {
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNo(3L);
        pageQuery.setPageSize(50L);

        Page<Object> page = MyBatisUtils.buildPage(pageQuery);

        assertThat(page.getCurrent()).isEqualTo(3L);
        assertThat(page.getSize()).isEqualTo(50L);
    }

    @Test
    void shouldBuildDefaultPageWhenQueryMissing() {
        Page<Object> page = MyBatisUtils.buildPage(null);

        assertThat(page.getCurrent()).isEqualTo(1L);
        assertThat(page.getSize()).isEqualTo(20L);
    }
}
