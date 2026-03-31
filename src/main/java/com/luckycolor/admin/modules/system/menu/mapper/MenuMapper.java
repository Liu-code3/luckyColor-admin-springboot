package com.luckycolor.admin.modules.system.menu.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuMapper extends BaseMapperX<MenuDO> {
}
