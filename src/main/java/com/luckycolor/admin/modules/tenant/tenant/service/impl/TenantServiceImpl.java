package com.luckycolor.admin.modules.tenant.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@TenantIgnore
@ConditionalOnBean(TenantMapper.class)
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;

    public TenantServiceImpl(TenantMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    @Override
    public PageResult<TenantPageResponse> pageTenants(TenantPageQuery query) {
        PageResult<TenantDO> pageResult = tenantMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(
            pageResult.getList().stream().map(this::toPageResponse).toList(),
            pageResult.getTotal()
        );
    }

    private LambdaQueryWrapper<TenantDO> buildQueryWrapper(TenantPageQuery query) {
        LambdaQueryWrapper<TenantDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getName()), TenantDO::getName, query.getName());
        queryWrapper.eq(query.getPackageId() != null, TenantDO::getPackageId, query.getPackageId());
        queryWrapper.eq(query.getStatus() != null, TenantDO::getStatus, query.getStatus());
        queryWrapper.orderByDesc(TenantDO::getCreateTime);
        return queryWrapper;
    }

    private TenantPageResponse toPageResponse(TenantDO tenant) {
        return new TenantPageResponse(
            tenant.getId(),
            tenant.getName(),
            tenant.getPackageId(),
            tenant.getContactName(),
            tenant.getContactMobile(),
            tenant.getAccountCount(),
            tenant.getExpireTime(),
            tenant.getStatus()
        );
    }
}
