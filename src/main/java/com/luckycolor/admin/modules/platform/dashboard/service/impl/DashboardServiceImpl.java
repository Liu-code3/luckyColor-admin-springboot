package com.luckycolor.admin.modules.platform.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luckycolor.admin.infrastructure.security.datascope.DataScopeConditionBuilder;
import com.luckycolor.admin.modules.iam.audit.dataobject.SecurityAuditLogDO;
import com.luckycolor.admin.modules.iam.audit.mapper.SecurityAuditLogMapper;
import com.luckycolor.admin.modules.platform.dashboard.service.DashboardService;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardOverviewResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardRecentVisitResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendPointResponse;
import com.luckycolor.admin.modules.platform.dashboard.web.response.DashboardVisitTrendResponse;
import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import com.luckycolor.admin.modules.system.menu.mapper.MenuMapper;
import com.luckycolor.admin.modules.system.notice.dataobject.NoticeDO;
import com.luckycolor.admin.modules.system.notice.mapper.NoticeMapper;
import com.luckycolor.admin.modules.system.role.dataobject.SystemRoleDO;
import com.luckycolor.admin.modules.system.role.mapper.SystemRoleMapper;
import com.luckycolor.admin.modules.system.user.dataobject.SystemUserDO;
import com.luckycolor.admin.modules.system.user.mapper.SystemUserMapper;
import com.luckycolor.admin.modules.tenant.tenant.dataobject.TenantDO;
import com.luckycolor.admin.modules.tenant.tenant.mapper.TenantMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean({
    TenantMapper.class,
    SystemUserMapper.class,
    SystemRoleMapper.class,
    MenuMapper.class,
    NoticeMapper.class,
    SecurityAuditLogMapper.class
})
public class DashboardServiceImpl implements DashboardService {

    private static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";

    private static final int ACTIVE_STATUS = 0;

    private static final int PUBLISHED_STATUS = 1;

    private static final int SUCCESS = 1;

    private final TenantMapper tenantMapper;
    private final SystemUserMapper systemUserMapper;
    private final SystemRoleMapper systemRoleMapper;
    private final MenuMapper menuMapper;
    private final NoticeMapper noticeMapper;
    private final SecurityAuditLogMapper securityAuditLogMapper;
    private final DataScopeConditionBuilder dataScopeConditionBuilder;

    public DashboardServiceImpl(
        TenantMapper tenantMapper,
        SystemUserMapper systemUserMapper,
        SystemRoleMapper systemRoleMapper,
        MenuMapper menuMapper,
        NoticeMapper noticeMapper,
        SecurityAuditLogMapper securityAuditLogMapper,
        DataScopeConditionBuilder dataScopeConditionBuilder
    ) {
        this.tenantMapper = tenantMapper;
        this.systemUserMapper = systemUserMapper;
        this.systemRoleMapper = systemRoleMapper;
        this.menuMapper = menuMapper;
        this.noticeMapper = noticeMapper;
        this.securityAuditLogMapper = securityAuditLogMapper;
        this.dataScopeConditionBuilder = dataScopeConditionBuilder;
    }

    @Override
    public DashboardOverviewResponse getOverview() {
        return new DashboardOverviewResponse(
            countActiveTenants(),
            countActiveUsers(),
            countActiveRoles(),
            countActiveMenus(),
            countPublishedNotices(),
            countTodayLogins()
        );
    }

    @Override
    public DashboardVisitTrendResponse getVisitTrend() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(6);
        LocalDateTime startTime = startDate.atStartOfDay();
        List<SecurityAuditLogDO> logs = securityAuditLogMapper.selectList(buildLoginLogWrapper(startTime, null));
        Map<LocalDate, long[]> dailyStatistics = initTrendMap(startDate, today);
        for (SecurityAuditLogDO log : logs) {
            if (log.getCreateTime() == null) {
                continue;
            }
            LocalDate day = log.getCreateTime().toLocalDate();
            long[] statistics = dailyStatistics.get(day);
            if (statistics == null) {
                continue;
            }
            statistics[0]++;
            if (SUCCESS == safeInt(log.getSuccess())) {
                statistics[1]++;
            } else {
                statistics[2]++;
            }
        }

        List<DashboardVisitTrendPointResponse> points = new ArrayList<>(dailyStatistics.size());
        long totalVisits = 0L;
        long successVisits = 0L;
        long failedVisits = 0L;
        for (Map.Entry<LocalDate, long[]> entry : dailyStatistics.entrySet()) {
            long total = entry.getValue()[0];
            long success = entry.getValue()[1];
            long failure = entry.getValue()[2];
            totalVisits += total;
            successVisits += success;
            failedVisits += failure;
            points.add(new DashboardVisitTrendPointResponse(entry.getKey(), total, success, failure));
        }
        return new DashboardVisitTrendResponse(points, totalVisits, successVisits, failedVisits);
    }

    @Override
    public List<DashboardRecentVisitResponse> listRecentVisits() {
        LambdaQueryWrapper<SecurityAuditLogDO> queryWrapper = buildLoginLogWrapper(null, SUCCESS);
        queryWrapper.orderByDesc(SecurityAuditLogDO::getCreateTime)
            .last("limit 10");
        return securityAuditLogMapper.selectList(queryWrapper).stream()
            .map(log -> new DashboardRecentVisitResponse(
                log.getId(),
                log.getTenantId(),
                log.getUserId(),
                log.getUsername(),
                log.getRequestUri(),
                log.getRemoteIp(),
                log.getCreateTime()
            ))
            .toList();
    }

    private long countActiveTenants() {
        LambdaQueryWrapper<TenantDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TenantDO::getStatus, ACTIVE_STATUS);
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, TenantDO::getId, null);
        return tenantMapper.selectCount(queryWrapper);
    }

    private long countActiveUsers() {
        LambdaQueryWrapper<SystemUserDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUserDO::getStatus, ACTIVE_STATUS);
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemUserDO::getTenantId, SystemUserDO::getDepartmentId);
        return systemUserMapper.selectCount(queryWrapper);
    }

    private long countActiveRoles() {
        LambdaQueryWrapper<SystemRoleDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemRoleDO::getStatus, ACTIVE_STATUS);
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SystemRoleDO::getTenantId, null);
        return systemRoleMapper.selectCount(queryWrapper);
    }

    private long countActiveMenus() {
        LambdaQueryWrapper<MenuDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MenuDO::getStatus, ACTIVE_STATUS);
        return menuMapper.selectCount(queryWrapper);
    }

    private long countPublishedNotices() {
        LambdaQueryWrapper<NoticeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(NoticeDO::getPublishStatus, PUBLISHED_STATUS);
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, NoticeDO::getTenantId, null);
        return noticeMapper.selectCount(queryWrapper);
    }

    private long countTodayLogins() {
        LambdaQueryWrapper<SecurityAuditLogDO> queryWrapper = buildLoginLogWrapper(LocalDate.now().atStartOfDay(), SUCCESS);
        return securityAuditLogMapper.selectCount(queryWrapper);
    }

    private LambdaQueryWrapper<SecurityAuditLogDO> buildLoginLogWrapper(LocalDateTime beginTime, Integer success) {
        LambdaQueryWrapper<SecurityAuditLogDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SecurityAuditLogDO::getEventType, LOGIN_SUCCESS);
        queryWrapper.eq(success != null, SecurityAuditLogDO::getSuccess, success);
        queryWrapper.ge(beginTime != null, SecurityAuditLogDO::getCreateTime, beginTime);
        dataScopeConditionBuilder.applyCurrentScope(queryWrapper, SecurityAuditLogDO::getTenantId, null);
        return queryWrapper;
    }

    private Map<LocalDate, long[]> initTrendMap(LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, long[]> trendMap = new LinkedHashMap<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            trendMap.put(cursor, new long[3]);
            cursor = cursor.plusDays(1);
        }
        return trendMap;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
