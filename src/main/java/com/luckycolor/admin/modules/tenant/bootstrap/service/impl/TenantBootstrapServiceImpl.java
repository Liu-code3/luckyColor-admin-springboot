package com.luckycolor.admin.modules.tenant.bootstrap.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.modules.tenant.audit.service.TenantAuditLogService;
import com.luckycolor.admin.modules.tenant.bootstrap.config.TenantBootstrapProperties;
import com.luckycolor.admin.modules.tenant.bootstrap.dataobject.TenantBootstrapRecordDO;
import com.luckycolor.admin.modules.tenant.bootstrap.mapper.TenantBootstrapRecordMapper;
import com.luckycolor.admin.modules.tenant.bootstrap.service.TenantBootstrapService;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapExecuteRequest;
import com.luckycolor.admin.modules.tenant.bootstrap.web.request.TenantBootstrapRecordPageQuery;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapRecordResponse;
import com.luckycolor.admin.modules.tenant.bootstrap.web.response.TenantBootstrapTemplateResponse;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@TenantIgnore
@ConditionalOnBean({TenantMapper.class, TenantBootstrapRecordMapper.class})
public class TenantBootstrapServiceImpl implements TenantBootstrapService {

    private static final int STATUS_SUCCESS = 1;

    private final TenantMapper tenantMapper;
    private final TenantBootstrapRecordMapper tenantBootstrapRecordMapper;
    private final TenantAuditLogService tenantAuditLogService;
    private final TenantBootstrapProperties tenantBootstrapProperties;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public TenantBootstrapServiceImpl(
        TenantMapper tenantMapper,
        TenantBootstrapRecordMapper tenantBootstrapRecordMapper,
        TenantAuditLogService tenantAuditLogService,
        TenantBootstrapProperties tenantBootstrapProperties,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.tenantMapper = tenantMapper;
        this.tenantBootstrapRecordMapper = tenantBootstrapRecordMapper;
        this.tenantAuditLogService = tenantAuditLogService;
        this.tenantBootstrapProperties = tenantBootstrapProperties;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public List<TenantBootstrapTemplateResponse> listTemplates() {
        return tenantBootstrapProperties.getTemplates().stream()
            .map(this::toTemplateResponse)
            .toList();
    }

    @Override
    public PageResult<TenantBootstrapRecordResponse> pageRecords(TenantBootstrapRecordPageQuery query) {
        PageResult<TenantBootstrapRecordDO> pageResult = tenantBootstrapRecordMapper.selectPageResult(
            query,
            buildPageQuery(query)
        );
        return PageResult.of(pageResult.getList().stream().map(this::toRecordResponse).toList(), pageResult.getTotal());
    }

    @Override
    public TenantBootstrapRecordResponse bootstrapTenant(Long tenantId, TenantBootstrapExecuteRequest request) {
        TenantDO tenant = getRequiredTenant(tenantId);
        TenantBootstrapProperties.Template template = getRequiredTemplate(request.getTemplateCode());
        validateBootstrapNotExecuted(tenantId);

        TenantBootstrapRecordDO record = new TenantBootstrapRecordDO();
        record.setTenantId(tenantId);
        record.setTemplateCode(template.getCode());
        record.setTemplateName(template.getName());
        record.setRoleCodes(joinCodes(template.getRoleCodes()));
        record.setMenuCodes(joinCodes(template.getMenuCodes()));
        record.setAdminUsername(resolveAdminUsername(request, template));
        record.setAdminNickname(resolveAdminNickname(request, template, tenant));
        record.setStatus(STATUS_SUCCESS);
        record.setBootstrapTime(LocalDateTime.now());
        record.setRemark(resolveRemark(request, template));
        tenantBootstrapRecordMapper.insert(record);
        tenantAuditLogService.record(tenantId, "TENANT_BOOTSTRAP", record.getId(), "BOOTSTRAP", template.getCode());
        return toRecordResponse(record);
    }

    private LambdaQueryWrapper<TenantBootstrapRecordDO> buildPageQuery(TenantBootstrapRecordPageQuery query) {
        LambdaQueryWrapper<TenantBootstrapRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(query.getTenantId() != null, TenantBootstrapRecordDO::getTenantId, query.getTenantId());
        queryWrapper.like(
            StringUtils.hasText(query.getTemplateCode()),
            TenantBootstrapRecordDO::getTemplateCode,
            query.getTemplateCode()
        );
        queryWrapper.eq(query.getStatus() != null, TenantBootstrapRecordDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, TenantBootstrapRecordDO::getTenantId, null);
        queryWrapper.orderByDesc(TenantBootstrapRecordDO::getBootstrapTime)
            .orderByDesc(TenantBootstrapRecordDO::getCreateTime);
        return queryWrapper;
    }

    private TenantDO getRequiredTenant(Long tenantId) {
        TenantDO tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant not found");
        }
        return tenant;
    }

    private TenantBootstrapProperties.Template getRequiredTemplate(String templateCode) {
        return tenantBootstrapProperties.getTemplates().stream()
            .filter(item -> StringUtils.hasText(item.getCode()))
            .filter(item -> item.getCode().equals(templateCode))
            .filter(item -> item.getStatus() == null || item.getStatus() == 0)
            .findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bootstrap template not found"));
    }

    private void validateBootstrapNotExecuted(Long tenantId) {
        LambdaQueryWrapper<TenantBootstrapRecordDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantBootstrapRecordDO::getTenantId, tenantId);
        Long count = tenantBootstrapRecordMapper.selectCount(queryWrapper);
        if (count != null && count > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Tenant already bootstrapped");
        }
    }

    private TenantBootstrapTemplateResponse toTemplateResponse(TenantBootstrapProperties.Template template) {
        return new TenantBootstrapTemplateResponse(
            template.getCode(),
            template.getName(),
            safeCodes(template.getRoleCodes()),
            safeCodes(template.getMenuCodes()),
            template.getAdminUsername(),
            template.getAdminNickname(),
            template.getStatus(),
            template.getRemark()
        );
    }

    private TenantBootstrapRecordResponse toRecordResponse(TenantBootstrapRecordDO record) {
        return new TenantBootstrapRecordResponse(
            record.getId(),
            record.getTenantId(),
            record.getTemplateCode(),
            record.getTemplateName(),
            splitCodes(record.getRoleCodes()),
            splitCodes(record.getMenuCodes()),
            record.getAdminUsername(),
            record.getAdminNickname(),
            record.getStatus(),
            record.getBootstrapTime(),
            record.getRemark()
        );
    }

    private List<String> safeCodes(List<String> codes) {
        return codes == null ? Collections.emptyList() : codes;
    }

    private String joinCodes(List<String> codes) {
        return String.join(",", safeCodes(codes));
    }

    private List<String> splitCodes(String codes) {
        if (!StringUtils.hasText(codes)) {
            return Collections.emptyList();
        }
        return List.of(codes.split(","));
    }

    private String resolveAdminUsername(
        TenantBootstrapExecuteRequest request,
        TenantBootstrapProperties.Template template
    ) {
        if (StringUtils.hasText(request.getAdminUsername())) {
            return request.getAdminUsername();
        }
        return template.getAdminUsername();
    }

    private String resolveAdminNickname(
        TenantBootstrapExecuteRequest request,
        TenantBootstrapProperties.Template template,
        TenantDO tenant
    ) {
        if (StringUtils.hasText(request.getAdminNickname())) {
            return request.getAdminNickname();
        }
        if (StringUtils.hasText(template.getAdminNickname())) {
            return template.getAdminNickname();
        }
        return tenant.getName() + "管理员";
    }

    private String resolveRemark(TenantBootstrapExecuteRequest request, TenantBootstrapProperties.Template template) {
        if (StringUtils.hasText(request.getRemark())) {
            return request.getRemark();
        }
        return template.getRemark();
    }
}
