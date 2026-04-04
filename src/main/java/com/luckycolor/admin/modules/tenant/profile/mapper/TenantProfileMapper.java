package com.luckycolor.admin.modules.tenant.profile.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.tenant.profile.dataobject.TenantProfileDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TenantProfileMapper extends BaseMapperX<TenantProfileDO> {
}
