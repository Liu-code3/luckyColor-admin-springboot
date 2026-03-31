package com.luckycolor.admin.modules.system.user.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemUserMapper extends BaseMapperX<SystemUserDO> {
}
