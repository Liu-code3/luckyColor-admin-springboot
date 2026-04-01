package com.luckycolor.admin.modules.system.config.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.config.dataobject.SystemConfigDO;
import com.luckycolor.admin.modules.system.config.mapper.SystemConfigMapper;
import com.luckycolor.admin.modules.system.config.service.SystemConfigService;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigPageQuery;
import com.luckycolor.admin.modules.system.config.web.request.SystemConfigSaveRequest;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigDetailResponse;
import com.luckycolor.admin.modules.system.config.web.response.SystemConfigPageResponse;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
@ConditionalOnBean(SystemConfigMapper.class)
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final String SENSITIVE_MASK = "******";

    private final SystemConfigMapper systemConfigMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public SystemConfigServiceImpl(
        SystemConfigMapper systemConfigMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.systemConfigMapper = systemConfigMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public PageResult<SystemConfigPageResponse> pageConfigs(SystemConfigPageQuery query) {
        PageResult<SystemConfigDO> pageResult = systemConfigMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public SystemConfigDetailResponse getConfig(Long id) {
        return toDetailResponse(getRequiredConfig(id));
    }

    @Override
    public Long createConfig(SystemConfigSaveRequest request) {
        ensureConfigKeyUnique(null, request.getConfigKey());
        SystemConfigDO systemConfig = new SystemConfigDO();
        fillConfig(systemConfig, request);
        systemConfigMapper.insert(systemConfig);
        return systemConfig.getId();
    }

    @Override
    public void updateConfig(Long id, SystemConfigSaveRequest request) {
        SystemConfigDO systemConfig = getRequiredConfig(id);
        ensureConfigKeyUnique(id, request.getConfigKey());
        fillConfig(systemConfig, request);
        systemConfigMapper.updateById(systemConfig);
    }

    private LambdaQueryWrapper<SystemConfigDO> buildQueryWrapper(SystemConfigPageQuery query) {
        LambdaQueryWrapper<SystemConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getConfigKey()), SystemConfigDO::getConfigKey, query.getConfigKey());
        queryWrapper.like(StringUtils.hasText(query.getConfigName()), SystemConfigDO::getConfigName, query.getConfigName());
        queryWrapper.eq(query.getSensitive() != null, SystemConfigDO::getSensitive, query.getSensitive());
        queryWrapper.eq(query.getStatus() != null, SystemConfigDO::getStatus, query.getStatus());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemConfigDO::getTenantId, null);
        queryWrapper.orderByAsc(SystemConfigDO::getSort)
            .orderByDesc(SystemConfigDO::getCreateTime);
        return queryWrapper;
    }

    private SystemConfigDO getRequiredConfig(Long id) {
        SystemConfigDO systemConfig = systemConfigMapper.selectById(id);
        if (systemConfig == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "System config not found");
        }
        return systemConfig;
    }

    private void ensureConfigKeyUnique(Long currentId, String configKey) {
        if (!StringUtils.hasText(configKey)) {
            return;
        }
        LambdaQueryWrapper<SystemConfigDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemConfigDO::getConfigKey, configKey.trim());
        List<SystemConfigDO> existingConfigs = systemConfigMapper.selectList(queryWrapper);
        boolean duplicated = existingConfigs.stream()
            .anyMatch(item -> currentId == null || !currentId.equals(item.getId()));
        if (duplicated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "System config key already exists");
        }
    }

    private void fillConfig(SystemConfigDO systemConfig, SystemConfigSaveRequest request) {
        systemConfig.setConfigKey(request.getConfigKey());
        systemConfig.setConfigName(request.getConfigName());
        systemConfig.setConfigValue(request.getConfigValue());
        systemConfig.setSensitive(request.getSensitive());
        systemConfig.setStatus(request.getStatus());
        systemConfig.setSort(request.getSort());
        systemConfig.setRemark(request.getRemark());
    }

    private SystemConfigPageResponse toPageResponse(SystemConfigDO systemConfig) {
        return new SystemConfigPageResponse(
            systemConfig.getId(),
            systemConfig.getTenantId(),
            systemConfig.getConfigKey(),
            systemConfig.getConfigName(),
            maskIfSensitive(systemConfig.getConfigValue(), systemConfig.getSensitive()),
            systemConfig.getSensitive(),
            systemConfig.getStatus(),
            systemConfig.getSort(),
            systemConfig.getRemark()
        );
    }

    private SystemConfigDetailResponse toDetailResponse(SystemConfigDO systemConfig) {
        return new SystemConfigDetailResponse(
            systemConfig.getId(),
            systemConfig.getTenantId(),
            systemConfig.getConfigKey(),
            systemConfig.getConfigName(),
            maskIfSensitive(systemConfig.getConfigValue(), systemConfig.getSensitive()),
            systemConfig.getSensitive(),
            systemConfig.getStatus(),
            systemConfig.getSort(),
            systemConfig.getRemark()
        );
    }

    private String maskIfSensitive(String configValue, Integer sensitive) {
        if (sensitive == null || sensitive == 0 || !StringUtils.hasText(configValue)) {
            return configValue;
        }
        return SENSITIVE_MASK;
    }
}
