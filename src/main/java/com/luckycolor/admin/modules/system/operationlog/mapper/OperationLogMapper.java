package com.luckycolor.admin.modules.system.operationlog.mapper;

import com.luckycolor.admin.infrastructure.persistence.mapper.BaseMapperX;
import com.luckycolor.admin.modules.system.operationlog.dataobject.OperationLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapperX<OperationLogDO> {
}
