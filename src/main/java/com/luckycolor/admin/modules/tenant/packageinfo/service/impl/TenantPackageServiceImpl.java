package com.luckycolor.admin.modules.tenant.packageinfo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.tenant.packageinfo.dataobject.TenantPackageDO;
import com.luckycolor.admin.modules.tenant.packageinfo.mapper.TenantPackageMapper;
import com.luckycolor.admin.modules.tenant.packageinfo.service.TenantPackageService;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackagePageQuery;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageSaveRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.request.TenantPackageStatusRequest;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackageDetailResponse;
import com.luckycolor.admin.modules.tenant.packageinfo.web.response.TenantPackagePageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

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

    @Override
    public TenantPackageDetailResponse getTenantPackage(Long id) {
        return toDetailResponse(getRequiredTenantPackage(id));
    }

    @Override
    public Long createTenantPackage(TenantPackageSaveRequest request) {
        TenantPackageDO tenantPackage = new TenantPackageDO();
        fillTenantPackage(tenantPackage, request);
        tenantPackageMapper.insert(tenantPackage);
        return tenantPackage.getId();
    }

    @Override
    public void updateTenantPackage(Long id, TenantPackageSaveRequest request) {
        TenantPackageDO tenantPackage = getRequiredTenantPackage(id);
        fillTenantPackage(tenantPackage, request);
        tenantPackageMapper.updateById(tenantPackage);
    }

    @Override
    public void updateTenantPackageStatus(Long id, TenantPackageStatusRequest request) {
        TenantPackageDO tenantPackage = getRequiredTenantPackage(id);
        tenantPackage.setStatus(request.getStatus());
        tenantPackageMapper.updateById(tenantPackage);
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

    private TenantPackageDetailResponse toDetailResponse(TenantPackageDO tenantPackage) {
        return new TenantPackageDetailResponse(
            tenantPackage.getId(),
            tenantPackage.getPackageName(),
            tenantPackage.getStatus(),
            tenantPackage.getSort(),
            tenantPackage.getRemark()
        );
    }

    private TenantPackageDO getRequiredTenantPackage(Long id) {
        TenantPackageDO tenantPackage = tenantPackageMapper.selectById(id);
        if (tenantPackage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant package not found");
        }
        return tenantPackage;
    }

    private void fillTenantPackage(TenantPackageDO tenantPackage, TenantPackageSaveRequest request) {
        tenantPackage.setPackageName(request.getPackageName());
        tenantPackage.setStatus(request.getStatus());
        tenantPackage.setSort(request.getSort());
        tenantPackage.setRemark(request.getRemark());
    }
}
