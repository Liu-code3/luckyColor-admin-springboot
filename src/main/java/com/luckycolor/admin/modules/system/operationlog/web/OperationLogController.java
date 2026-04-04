package com.luckycolor.admin.modules.system.operationlog.web;

import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.FORBIDDEN;
import static com.luckycolor.admin.common.config.OpenApiExamplePayloads.UNAUTHORIZED;

import com.luckycolor.admin.common.api.ApiResponse;
import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.infrastructure.security.authorization.RequirePermission;
import com.luckycolor.admin.modules.system.operationlog.mapper.OperationLogMapper;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogService;
import com.luckycolor.admin.modules.system.operationlog.web.request.OperationLogPageQuery;
import com.luckycolor.admin.modules.system.operationlog.web.response.OperationLogPageResponse;
import com.luckycolor.admin.common.config.ConditionalOnPersistenceEnabled;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/operation-logs")
@ConditionalOnPersistenceEnabled
@Tag(name = "Operation Logs", description = "Operation log query APIs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @GetMapping("/page")
    @RequirePermission("system:operation-log:query")
    @Operation(summary = "Page operation logs")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Operation logs loaded"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = UNAUTHORIZED))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Permission denied",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = FORBIDDEN))
        )
    })
    public ApiResponse<PageResult<OperationLogPageResponse>> page(@ParameterObject OperationLogPageQuery query) {
        return ApiResponse.success(operationLogService.pageOperationLogs(query));
    }
}
