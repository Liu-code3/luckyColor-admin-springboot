package com.luckycolor.admin.common.page;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PageQueryTest {

    @Test
    void shouldUseDefaultValuesWhenInputInvalid() {
        PageQuery query = new PageQuery();
        query.setPageNo(0L);
        query.setPageSize(-1L);

        assertThat(query.resolvePageNo()).isEqualTo(1L);
        assertThat(query.resolvePageSize()).isEqualTo(20L);
    }

    @Test
    void shouldCapPageSizeAtMaxLimit() {
        PageQuery query = new PageQuery();
        query.setPageSize(500L);

        assertThat(query.resolvePageSize()).isEqualTo(200L);
    }
}
