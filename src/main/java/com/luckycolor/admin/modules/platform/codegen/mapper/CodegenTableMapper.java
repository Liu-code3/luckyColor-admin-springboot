package com.luckycolor.admin.modules.platform.codegen.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.platform.codegen.dataobject.CodegenTableDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CodegenTableMapper extends BaseMapperX<CodegenTableDO> {
}
