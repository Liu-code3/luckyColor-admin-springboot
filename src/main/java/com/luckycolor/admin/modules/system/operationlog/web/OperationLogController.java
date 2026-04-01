package com.luckycolor.admin.modules.system.operationlog.web;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.operationlog.mapper.OperationLogMapper;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogService;
import com.luckycolor.admin.modules.system.operationlog.web.request.OperationLogPageQuery;
import com.luckycolor.admin.modules.system.operationlog.web.response.OperationLogPageResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/operation-logs")
@ConditionalOnBean(OperationLogMapper.class)
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping("/page")
    @RequirePermission("system:operation-log:query")
    public ApiResponse<PageResult<OperationLogPageResponse>> page(OperationLogPageQuery query) {
        return ApiResponse.success(operationLogService.pageOperationLogs(query));
    }
}
