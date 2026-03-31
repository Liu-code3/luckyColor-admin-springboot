package com.luckycolor.admin.modules.tenant.tenant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import com.luckycolor.admin.modules.tenant.tenant.service.TenantService;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantExpireTimeRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantPageQuery;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantSaveRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.request.TenantStatusRequest;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantDetailResponse;
import com.luckycolor.admin.modules.tenant.tenant.web.response.TenantPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@TenantIgnore
@ConditionalOnBean(TenantMapper.class)
public class TenantServiceImpl implements TenantService {

    private final TenantMapper tenantMapper;
    private final TenantAuditLogService tenantAuditLogService;

    public TenantServiceImpl(TenantMapper tenantMapper, TenantAuditLogService tenantAuditLogService) {
        this.tenantMapper = tenantMapper;
        this.tenantAuditLogService = tenantAuditLogService;
    }

    @Override
    public PageResult<TenantPageResponse> pageTenants(TenantPageQuery query) {
        PageResult<TenantDO> pageResult = tenantMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(
            pageResult.getList().stream().map(this::toPageResponse).toList(),
            pageResult.getTotal()
        );
    }

    @Override
    public TenantDetailResponse getTenant(Long id) {
        return toDetailResponse(getRequiredTenant(id));
    }

    @Override
    public Long createTenant(TenantSaveRequest request) {
        TenantDO tenant = new TenantDO();
        fillTenant(tenant, request);
        tenantMapper.insert(tenant);
        tenantAuditLogService.record(tenant.getId(), "TENANT", tenant.getId(), "CREATE", tenant.getName());
        return tenant.getId();
    }

    @Override
    public void updateTenant(Long id, TenantSaveRequest request) {
        TenantDO tenant = getRequiredTenant(id);
        fillTenant(tenant, request);
        tenantMapper.updateById(tenant);
        tenantAuditLogService.record(tenant.getId(), "TENANT", tenant.getId(), "UPDATE", tenant.getName());
    }

    @Override
    public void updateTenantStatus(Long id, TenantStatusRequest request) {
        TenantDO tenant = getRequiredTenant(id);
        tenant.setStatus(request.getStatus());
        tenantMapper.updateById(tenant);
        tenantAuditLogService.record(tenant.getId(), "TENANT", tenant.getId(), "UPDATE_STATUS", String.valueOf(request.getStatus()));
    }

    @Override
    public void updateTenantExpireTime(Long id, TenantExpireTimeRequest request) {
        TenantDO tenant = getRequiredTenant(id);
        tenant.setExpireTime(request.getExpireTime());
        tenantMapper.updateById(tenant);
        tenantAuditLogService.record(tenant.getId(), "TENANT", tenant.getId(), "UPDATE_EXPIRE_TIME", String.valueOf(request.getExpireTime()));
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

    private TenantDetailResponse toDetailResponse(TenantDO tenant) {
        return new TenantDetailResponse(
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

    private TenantDO getRequiredTenant(Long id) {
        TenantDO tenant = tenantMapper.selectById(id);
        if (tenant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found");
        }
        return tenant;
    }

    private void fillTenant(TenantDO tenant, TenantSaveRequest request) {
        tenant.setName(request.getName());
        tenant.setPackageId(request.getPackageId());
        tenant.setContactName(request.getContactName());
        tenant.setContactMobile(request.getContactMobile());
        tenant.setAccountCount(request.getAccountCount());
        tenant.setExpireTime(request.getExpireTime());
        tenant.setStatus(request.getStatus());
    }
}
