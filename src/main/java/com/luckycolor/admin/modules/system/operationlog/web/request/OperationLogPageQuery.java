package com.luckycolor.admin.modules.system.operationlog.web.request;

import com.luckycolor.admin.common.page.PageQuery;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class OperationLogPageQuery extends PageQuery {

    private Long tenantId;

    private String username;

    private String bizModule;

    private String operationType;

    private String requestMethod;

    private String requestUri;

    private Integer success;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
