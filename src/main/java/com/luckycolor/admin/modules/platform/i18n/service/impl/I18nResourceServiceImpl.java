package com.luckycolor.admin.modules.platform.i18n.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.platform.i18n.dataobject.I18nResourceDO;
import com.luckycolor.admin.modules.platform.i18n.mapper.I18nResourceMapper;
import com.luckycolor.admin.modules.platform.i18n.service.I18nResourceService;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourcePageQuery;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceSaveRequest;
import com.luckycolor.admin.modules.platform.i18n.web.request.I18nResourceStatusRequest;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourceDetailResponse;
import com.luckycolor.admin.modules.platform.i18n.web.response.I18nResourcePageResponse;
import java.util.List;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnPersistenceEnabled
public class I18nResourceServiceImpl implements I18nResourceService {

    private static final int DEFAULT_VERSION = 1;

    private final I18nResourceMapper i18nResourceMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public I18nResourceServiceImpl(
        I18nResourceMapper i18nResourceMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.i18nResourceMapper = i18nResourceMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public PageResult<I18nResourcePageResponse> pageResources(I18nResourcePageQuery query) {
        PageResult<I18nResourceDO> pageResult = i18nResourceMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public I18nResourceDetailResponse getResource(Long id) {
        return toDetailResponse(getRequiredResource(id));
    }

    @Override
    public Long createResource(I18nResourceSaveRequest request) {
        ensureUnique(null, request);
        I18nResourceDO resource = new I18nResourceDO();
        fillResource(resource, request);
        resource.setVersion(DEFAULT_VERSION);
        i18nResourceMapper.insert(resource);
        return resource.getId();
    }

    @Override
    public void updateResource(Long id, I18nResourceSaveRequest request) {
        I18nResourceDO resource = getRequiredResource(id);
        ensureUnique(id, request);
        fillResource(resource, request);
        i18nResourceMapper.updateById(resource);
    }

    @Override
    public void updateStatus(Long id, I18nResourceStatusRequest request) {
        I18nResourceDO resource = getRequiredResource(id);
        resource.setStatus(request.getStatus());
        i18nResourceMapper.updateById(resource);
    }

    @Override
    public void bumpVersion(Long id) {
        I18nResourceDO resource = getRequiredResource(id);
        int currentVersion = resource.getVersion() == null || resource.getVersion() < 1
            ? DEFAULT_VERSION
            : resource.getVersion();
        resource.setVersion(currentVersion + 1);
        i18nResourceMapper.updateById(resource);
    }

    private LambdaQueryWrapper<I18nResourceDO> buildQueryWrapper(I18nResourcePageQuery query) {
        LambdaQueryWrapper<I18nResourceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(StringUtils.hasText(query.getLocale()), I18nResourceDO::getLocale, trim(query.getLocale()));
        queryWrapper.eq(StringUtils.hasText(query.getNamespace()), I18nResourceDO::getNamespace, trim(query.getNamespace()));
        queryWrapper.like(
            StringUtils.hasText(query.getResourceKey()),
            I18nResourceDO::getResourceKey,
            trim(query.getResourceKey())
        );
        queryWrapper.eq(query.getStatus() != null, I18nResourceDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, I18nResourceDO::getTenantId, null);
        queryWrapper.orderByAsc(I18nResourceDO::getLocale)
            .orderByAsc(I18nResourceDO::getNamespace)
            .orderByAsc(I18nResourceDO::getResourceKey);
        return queryWrapper;
    }

    private I18nResourceDO getRequiredResource(Long id) {
        I18nResourceDO resource = i18nResourceMapper.selectById(id);
        if (resource == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "I18n resource not found");
        }
        return resource;
    }

    private void ensureUnique(Long currentId, I18nResourceSaveRequest request) {
        LambdaQueryWrapper<I18nResourceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(I18nResourceDO::getLocale, trim(request.getLocale()))
            .eq(I18nResourceDO::getNamespace, trim(request.getNamespace()))
            .eq(I18nResourceDO::getResourceKey, trim(request.getResourceKey()));
        List<I18nResourceDO> resources = i18nResourceMapper.selectList(queryWrapper);
        boolean duplicated = resources.stream()
            .anyMatch(item -> currentId == null || !currentId.equals(item.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "I18n resource already exists");
        }
    }

    private void fillResource(I18nResourceDO resource, I18nResourceSaveRequest request) {
        resource.setLocale(trim(request.getLocale()));
        resource.setNamespace(trim(request.getNamespace()));
        resource.setResourceKey(trim(request.getResourceKey()));
        resource.setResourceValue(request.getResourceValue());
        resource.setStatus(request.getStatus());
        resource.setRemark(request.getRemark());
        if (resource.getVersion() == null || resource.getVersion() < 1) {
            resource.setVersion(DEFAULT_VERSION);
        }
    }

    private I18nResourcePageResponse toPageResponse(I18nResourceDO resource) {
        return new I18nResourcePageResponse(
            resource.getId(),
            resource.getTenantId(),
            resource.getLocale(),
            resource.getNamespace(),
            resource.getResourceKey(),
            resource.getResourceValue(),
            resource.getVersion(),
            resource.getStatus(),
            resource.getRemark()
        );
    }

    private I18nResourceDetailResponse toDetailResponse(I18nResourceDO resource) {
        return new I18nResourceDetailResponse(
            resource.getId(),
            resource.getTenantId(),
            resource.getLocale(),
            resource.getNamespace(),
            resource.getResourceKey(),
            resource.getResourceValue(),
            resource.getVersion(),
            resource.getStatus(),
            resource.getRemark()
        );
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
