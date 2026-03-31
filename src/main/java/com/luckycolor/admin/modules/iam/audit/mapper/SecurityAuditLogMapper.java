package com.luckycolor.admin.modules.iam.audit.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.iam.audit.dataobject.SecurityAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SecurityAuditLogMapper extends BaseMapperX<SecurityAuditLogDO> {
}
