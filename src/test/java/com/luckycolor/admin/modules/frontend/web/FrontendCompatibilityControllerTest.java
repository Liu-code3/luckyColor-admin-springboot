package com.luckycolor.admin.modules.frontend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import com.luckycolor.admin.modules.platform.dashboard.service.DashboardService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class FrontendCompatibilityControllerTest {

    @Test
    void shouldFallbackDashboardOverviewWhenNativeDashboardServiceFails() {
        AuthUserService authUserService = Mockito.mock(AuthUserService.class);
        DashboardService dashboardService = Mockito.mock(DashboardService.class);
        AuthUser user = new AuthUser(
            1L,
            "admin",
            "123456",
            1L,
            "System Admin",
            0,
            List.of("ROLE_SUPER_ADMIN"),
            List.of("dashboard:query"),
            "ALL",
            null,
            List.of(),
            List.of()
        );
        when(authUserService.getByUserId(1L)).thenReturn(user);
        when(dashboardService.getOverview()).thenThrow(new IllegalStateException("boom"));
        when(dashboardService.getVisitTrend()).thenThrow(new IllegalStateException("boom"));
        when(dashboardService.listRecentVisits()).thenThrow(new IllegalStateException("boom"));

        FrontendCompatibilityController controller = new FrontendCompatibilityController(authUserService, dashboardService);

        FrontendCompatibilityController.LegacyDashboardOverviewResponse response =
            controller.dashboardOverview(buildAuthentication()).data();

        assertThat(response.user().username()).isEqualTo("admin");
        assertThat(response.stats().onlineUsers()).isZero();
        assertThat(response.stats().visitorUv()).isZero();
        assertThat(response.stats().pageViews()).isZero();
        assertThat(response.trend()).isEmpty();
        assertThat(response.recentVisits()).isEmpty();
        assertThat(response.notices()).isEmpty();
    }

    private Authentication buildAuthentication() {
        JwtAuthenticatedUser principal = new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN"));
        return new UsernamePasswordAuthenticationToken(principal, "jwt-token", List.of());
    }
}
