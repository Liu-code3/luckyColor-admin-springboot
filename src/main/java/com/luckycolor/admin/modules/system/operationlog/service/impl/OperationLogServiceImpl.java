package com.luckycolor.admin.modules.system.operationlog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.system.operationlog.dataobject.OperationLogDO;
import com.luckycolor.admin.modules.system.operationlog.mapper.OperationLogMapper;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogRecordCommand;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogService;
import com.luckycolor.admin.modules.system.operationlog.web.request.OperationLogPageQuery;
import com.luckycolor.admin.modules.system.operationlog.web.response.OperationLogPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@ConditionalOnBean(OperationLogMapper.class)
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public OperationLogServiceImpl(
        OperationLogMapper operationLogMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.operationLogMapper = operationLogMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public PageResult<OperationLogPageResponse> pageOperationLogs(OperationLogPageQuery query) {
        PageResult<OperationLogDO> pageResult = operationLogMapper.selectPageResult(query, buildQueryWrapper(query));
        return PageResult.of(pageResult.getList().stream().map(this::toPageResponse).toList(), pageResult.getTotal());
    }

    @Override
    public void record(OperationLogRecordCommand command) {
        OperationLogDO operationLog = new OperationLogDO();
        operationLog.setTenantId(command.tenantId());
        operationLog.setUserId(command.userId());
        operationLog.setUsername(command.username());
        operationLog.setBizModule(command.bizModule());
        operationLog.setOperationType(command.operationType());
        operationLog.setRequestMethod(command.requestMethod());
        operationLog.setRequestUri(command.requestUri());
        operationLog.setRequestParams(command.requestParams());
        operationLog.setSuccess(command.success());
        operationLog.setStatusCode(command.statusCode());
        operationLog.setDurationMs(command.durationMs());
        operationLog.setRemoteIp(command.remoteIp());
        operationLog.setErrorMessage(command.errorMessage());
        operationLogMapper.insert(operationLog);
    }

    private LambdaQueryWrapper<OperationLogDO> buildQueryWrapper(OperationLogPageQuery query) {
        LambdaQueryWrapper<OperationLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(query.getTenantId() != null, OperationLogDO::getTenantId, query.getTenantId());
        queryWrapper.like(StringUtils.hasText(query.getUsername()), OperationLogDO::getUsername, query.getUsername());
        queryWrapper.eq(StringUtils.hasText(query.getBizModule()), OperationLogDO::getBizModule, query.getBizModule());
        queryWrapper.eq(
            StringUtils.hasText(query.getOperationType()),
            OperationLogDO::getOperationType,
            query.getOperationType()
        );
        queryWrapper.eq(
            StringUtils.hasText(query.getRequestMethod()),
            OperationLogDO::getRequestMethod,
            query.getRequestMethod()
        );
        queryWrapper.like(StringUtils.hasText(query.getRequestUri()), OperationLogDO::getRequestUri, query.getRequestUri());
        queryWrapper.eq(query.getSuccess() != null, OperationLogDO::getSuccess, query.getSuccess());
        queryWrapper.ge(query.getBeginTime() != null, OperationLogDO::getCreateTime, query.getBeginTime());
        queryWrapper.le(query.getEndTime() != null, OperationLogDO::getCreateTime, query.getEndTime());
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, OperationLogDO::getTenantId, null);
        queryWrapper.orderByDesc(OperationLogDO::getCreateTime);
        return queryWrapper;
    }

    private OperationLogPageResponse toPageResponse(OperationLogDO operationLog) {
        return new OperationLogPageResponse(
            operationLog.getId(),
            operationLog.getTenantId(),
            operationLog.getUserId(),
            operationLog.getUsername(),
            operationLog.getBizModule(),
            operationLog.getOperationType(),
            operationLog.getRequestMethod(),
            operationLog.getRequestUri(),
            operationLog.getRequestParams(),
            operationLog.getSuccess(),
            operationLog.getStatusCode(),
            operationLog.getDurationMs(),
            operationLog.getRemoteIp(),
            operationLog.getErrorMessage(),
            operationLog.getCreateTime()
        );
    }
}
