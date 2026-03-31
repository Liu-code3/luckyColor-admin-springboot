package com.luckycolor.admin.common.page;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

    @Test
    void shouldConvertFromMyBatisPage() {
        Page<String> page = new Page<>(1, 20);
        page.setRecords(List.of("a", "b"));
        page.setTotal(2L);

        PageResult<String> result = PageResult.of(page);

        assertThat(result.getList()).containsExactly("a", "b");
        assertThat(result.getTotal()).isEqualTo(2L);
    }
}
