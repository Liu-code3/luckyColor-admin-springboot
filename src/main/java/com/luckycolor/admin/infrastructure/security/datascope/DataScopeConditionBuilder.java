package com.luckycolor.admin.infrastructure.security.datascope;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class DataScopeConditionBuilder {

    private final CurrentDataScopeResolver currentDataScopeResolver;

    public DataScopeConditionBuilder(CurrentDataScopeResolver currentDataScopeResolver) {
        this.currentDataScopeResolver = currentDataScopeResolver;
    }

    public <T> void applyCurrentScope(
        LambdaQueryWrapper<T> queryWrapper,
        @Nullable SFunction<T, ?> tenantColumn,
        @Nullable SFunction<T, ?> departmentColumn
    ) {
        Optional<DataScopeRule> currentRule = currentDataScopeResolver.resolveCurrentRule();
        currentRule.ifPresent(rule -> apply(queryWrapper, rule, tenantColumn, departmentColumn));
    }

    public <T> void apply(
        LambdaQueryWrapper<T> queryWrapper,
        DataScopeRule rule,
        @Nullable SFunction<T, ?> tenantColumn,
        @Nullable SFunction<T, ?> departmentColumn
    ) {
        if (rule == null || rule.type() == DataScopeType.ALL) {
            return;
        }

        switch (rule.type()) {
            case TENANT -> applyTenantScope(queryWrapper, rule, tenantColumn);
            case DEPARTMENT -> applyDepartmentScope(queryWrapper, rule, departmentColumn, false);
            case DEPARTMENT_AND_CHILDREN -> applyDepartmentScope(queryWrapper, rule, departmentColumn, true);
            case CUSTOM -> applyCustomScope(queryWrapper, rule, tenantColumn, departmentColumn);
            default -> {
            }
        }
    }

    private <T> void applyTenantScope(
        LambdaQueryWrapper<T> queryWrapper,
        DataScopeRule rule,
        @Nullable SFunction<T, ?> tenantColumn
    ) {
        if (tenantColumn == null) {
            denyAll(queryWrapper);
            return;
        }
        if (rule.tenantId() != null) {
            queryWrapper.eq(tenantColumn, rule.tenantId());
            return;
        }
        if (!rule.tenantIds().isEmpty()) {
            queryWrapper.in(tenantColumn, rule.tenantIds());
            return;
        }
        denyAll(queryWrapper);
    }

    private <T> void applyDepartmentScope(
        LambdaQueryWrapper<T> queryWrapper,
        DataScopeRule rule,
        @Nullable SFunction<T, ?> departmentColumn,
        boolean allowChildren
    ) {
        if (departmentColumn == null) {
            denyAll(queryWrapper);
            return;
        }
        List<Long> departmentIds = resolveDepartmentIds(rule, allowChildren);
        if (departmentIds.isEmpty()) {
            denyAll(queryWrapper);
            return;
        }
        if (departmentIds.size() == 1) {
            queryWrapper.eq(departmentColumn, departmentIds.get(0));
            return;
        }
        queryWrapper.in(departmentColumn, departmentIds);
    }

    private <T> void applyCustomScope(
        LambdaQueryWrapper<T> queryWrapper,
        DataScopeRule rule,
        @Nullable SFunction<T, ?> tenantColumn,
        @Nullable SFunction<T, ?> departmentColumn
    ) {
        boolean applied = false;
        if (tenantColumn != null && !rule.tenantIds().isEmpty()) {
            if (rule.tenantIds().size() == 1) {
                queryWrapper.eq(tenantColumn, rule.tenantIds().get(0));
            } else {
                queryWrapper.in(tenantColumn, rule.tenantIds());
            }
            applied = true;
        }
        if (departmentColumn != null && !rule.departmentIds().isEmpty()) {
            if (rule.departmentIds().size() == 1) {
                queryWrapper.eq(departmentColumn, rule.departmentIds().get(0));
            } else {
                queryWrapper.in(departmentColumn, rule.departmentIds());
            }
            applied = true;
        }
        if (!applied) {
            denyAll(queryWrapper);
        }
    }

    private <T> void denyAll(LambdaQueryWrapper<T> queryWrapper) {
        queryWrapper.apply("1 = 0");
    }

    private List<Long> resolveDepartmentIds(DataScopeRule rule, boolean allowChildren) {
        if (allowChildren && rule.departmentIds() != null && !rule.departmentIds().isEmpty()) {
            return rule.departmentIds();
        }
        List<Long> departmentIds = new ArrayList<>();
        if (rule.departmentId() != null) {
            departmentIds.add(rule.departmentId());
            return departmentIds;
        }
        if (rule.departmentIds() != null && !rule.departmentIds().isEmpty()) {
            departmentIds.add(rule.departmentIds().get(0));
        }
        return departmentIds;
    }
}
