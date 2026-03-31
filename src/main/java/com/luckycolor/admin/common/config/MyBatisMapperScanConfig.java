package com.luckycolor.admin.common.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan(basePackages = "com.luckycolor.admin", annotationClass = Mapper.class)
@ConditionalOnProperty(
    prefix = "app.persistence",
    name = "mapper-scan-enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class MyBatisMapperScanConfig {
}
