package com.luckycolor.admin.modules.tenant.packageinfo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.tenant.packageinfo.dataobject.TenantPackageDO;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@TenantIgnore
@ConditionalOnBean(TenantPackageMapper.class)
public class TenantPackageServiceImpl implements TenantPackageService {

    private final TenantPackageMapper tenantPackageMapper;

    public TenantPackageServiceImpl(TenantPackageMapper tenantPackageMapper) {
        this.tenantPackageMapper = tenantPackageMapper;
    }

    @Override
    public PageResult<TenantPackagePageResponse> pageTenantPackages(TenantPackagePageQuery query) {
        PageResult<TenantPackageDO> pageResult = tenantPackageMapper.selectPageResult(
            query,
            buildQueryWrapper(query)
        );
        return PageResult.of(
            pageResult.getList().stream().map(this::toPageResponse).toList(),
            pageResult.getTotal()
        );
    }

    private LambdaQueryWrapper<TenantPackageDO> buildQueryWrapper(TenantPackagePageQuery query) {
        LambdaQueryWrapper<TenantPackageDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(
            StringUtils.hasText(query.getPackageName()),
            TenantPackageDO::getPackageName,
            query.getPackageName()
        );
        queryWrapper.eq(query.getStatus() != null, TenantPackageDO::getStatus, query.getStatus());
        queryWrapper.orderByAsc(TenantPackageDO::getSort)
            .orderByDesc(TenantPackageDO::getCreateTime);
        return queryWrapper;
    }

    private TenantPackagePageResponse toPageResponse(TenantPackageDO tenantPackage) {
        return new TenantPackagePageResponse(
            tenantPackage.getId(),
            tenantPackage.getPackageName(),
            tenantPackage.getStatus(),
            tenantPackage.getSort(),
            tenantPackage.getRemark()
        );
    }
}
