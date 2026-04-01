package com.luckycolor.admin.modules.platform.codegen.service;

import com.luckycolor.admin.common.page.PageResult;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenDiscoveryQuery;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenImportRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenTablePageQuery;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenTableSaveRequest;
import com.luckycolor.admin.modules.platform.codegen.web.request.CodegenColumnsSaveRequest;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenDiscoveryTableResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTableDetailResponse;
import com.luckycolor.admin.modules.platform.codegen.web.response.CodegenTablePageResponse;

public interface CodegenMetadataService {

    PageResult<CodegenDiscoveryTableResponse> pageDiscoveryTables(CodegenDiscoveryQuery query);

    int importTables(Long tenantId, CodegenImportRequest request);

    PageResult<CodegenTablePageResponse> pageTables(CodegenTablePageQuery query);

    CodegenTableDetailResponse getTable(Long id);

    void updateTable(Long id, CodegenTableSaveRequest request);

    void updateColumns(Long id, CodegenColumnsSaveRequest request);
}
