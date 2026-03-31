package com.luckycolor.admin.modules.tenant.audit.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.tenant.audit.dataobject.TenantAuditLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantAuditLogMapper extends BaseMapperX<TenantAuditLogDO> {
}
