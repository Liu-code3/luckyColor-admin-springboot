package com.luckycolor.admin.modules.system.operationlog.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.system.operationlog.web.request.OperationLogPageQuery;
import com.luckycolor.admin.modules.system.operationlog.web.response.OperationLogPageResponse;

public interface OperationLogService {

    PageResult<OperationLogPageResponse> pageOperationLogs(OperationLogPageQuery query);

    void record(OperationLogRecordCommand command);
}
