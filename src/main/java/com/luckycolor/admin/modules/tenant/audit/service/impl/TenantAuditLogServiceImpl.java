package com.luckycolor.admin.modules.tenant.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.tenant.audit.dataobject.TenantAuditLogDO;
import com.luckycolor.admin.modules.tenant.audit.mapper.TenantAuditLogMapper;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.audit.web.request.TenantAuditLogPageQuery;
import com.luckycolor.admin.modules.tenant.audit.web.response.TenantAuditLogPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@TenantIgnore
@ConditionalOnBean(TenantAuditLogMapper.class)
public class TenantAuditLogServiceImpl implements TenantAuditLogService {

    private final TenantAuditLogMapper tenantAuditLogMapper;

    public TenantAuditLogServiceImpl(TenantAuditLogMapper tenantAuditLogMapper) {
        this.tenantAuditLogMapper = tenantAuditLogMapper;
    }

    @Override
    public PageResult<TenantAuditLogPageResponse> pageTenantAuditLogs(TenantAuditLogPageQuery query) {
        PageResult<TenantAuditLogDO> pageResult = tenantAuditLogMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(
            pageResult.getList().stream().map(this::toPageResponse).toList(),
            pageResult.getTotal()
        );
    }

    @Override
    public void record(Long tenantId, String targetType, Long targetId, String action, String content) {
        TenantAuditLogDO auditLog = new TenantAuditLogDO();
        auditLog.setTenantId(tenantId);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setAction(action);
        auditLog.setContent(content);
        tenantAuditLogMapper.insert(auditLog);
    }

    private LambdaQueryWrapper<TenantAuditLogDO> buildQueryWrapper(TenantAuditLogPageQuery query) {
        LambdaQueryWrapper<TenantAuditLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(query.getTenantId() != null, TenantAuditLogDO::getTenantId, query.getTenantId());
        queryWrapper.eq(StringUtils.hasText(query.getTargetType()), TenantAuditLogDO::getTargetType, query.getTargetType());
        queryWrapper.eq(StringUtils.hasText(query.getAction()), TenantAuditLogDO::getAction, query.getAction());
        queryWrapper.orderByDesc(TenantAuditLogDO::getCreateTime);
        return queryWrapper;
    }

    private TenantAuditLogPageResponse toPageResponse(TenantAuditLogDO auditLog) {
        return new TenantAuditLogPageResponse(
            auditLog.getId(),
            auditLog.getTenantId(),
            auditLog.getTargetType(),
            auditLog.getTargetId(),
            auditLog.getAction(),
            auditLog.getContent(),
            auditLog.getCreateTime()
        );
    }
}
