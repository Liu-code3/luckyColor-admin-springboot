package com.luckycolor.admin.modules.platform.codegen.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.platform.codegen.dataobject.CodegenColumnDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodegenColumnMapper extends BaseMapperX<CodegenColumnDO> {
}
