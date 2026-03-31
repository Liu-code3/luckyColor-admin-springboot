package com.luckycolor.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.luckycolor.admin.common.page.PageQuery;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.persistence.util.MyBatisUtils;

public interface BaseMapperX<T> extends BaseMapper<T> {

    default PageResult<T> selectPageResult(PageQuery pageQuery, Wrapper<T> queryWrapper) {
        IPage<T> page = selectPage(MyBatisUtils.buildPage(pageQuery), queryWrapper);
        return PageResult.of(page);
    }
}
