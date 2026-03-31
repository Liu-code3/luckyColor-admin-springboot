package com.luckycolor.admin.support;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;

public final class MyBatisTableInfoTestUtils {

    private MyBatisTableInfoTestUtils() {
    }

    public static void initTableInfo(Class<?> entityClass) {
        if (TableInfoHelper.getTableInfo(entityClass) != null) {
            return;
        }
        TableInfoHelper.initTableInfo(
            new MapperBuilderAssistant(new MybatisConfiguration(), entityClass.getName() + "Mapper"),
            entityClass
        );
    }
}
