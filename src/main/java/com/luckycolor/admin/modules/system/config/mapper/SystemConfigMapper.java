package com.luckycolor.admin.modules.system.config.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.system.config.dataobject.SystemConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemConfigMapper extends BaseMapperX<SystemConfigDO> {
}
