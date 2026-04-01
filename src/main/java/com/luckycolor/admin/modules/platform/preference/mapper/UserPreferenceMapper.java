package com.luckycolor.admin.modules.platform.preference.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.platform.preference.dataobject.UserPreferenceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserPreferenceMapper extends BaseMapperX<UserPreferenceDO> {
}
