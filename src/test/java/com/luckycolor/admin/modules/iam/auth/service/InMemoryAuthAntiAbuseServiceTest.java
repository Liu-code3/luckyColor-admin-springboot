package com.luckycolor.admin.modules.iam.auth.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.luckycolor.admin.modules.iam.auth.config.AuthAntiAbuseProperties;
import com.luckycolor.admin.modules.iam.auth.service.impl.InMemoryAuthAntiAbuseService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class InMemoryAuthAntiAbuseServiceTest {

    @Test
    void shouldLockLoginAfterConfiguredFailures() {
        AuthAntiAbuseProperties properties = new AuthAntiAbuseProperties();
        InMemoryAuthAntiAbuseService service = new InMemoryAuthAntiAbuseService(properties);

        for (int i = 0; i < properties.getLogin().getMaxFailures(); i++) {
            service.recordLoginFailure("tenant_001", "admin");
        }

        assertThatThrownBy(() -> service.checkLoginAllowed("tenant_001", "admin"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("429 TOO_MANY_REQUESTS");
    }

    @Test
    void shouldClearLoginFailuresAfterSuccess() {
        AuthAntiAbuseProperties properties = new AuthAntiAbuseProperties();
        InMemoryAuthAntiAbuseService service = new InMemoryAuthAntiAbuseService(properties);

        for (int i = 0; i < properties.getLogin().getMaxFailures() - 1; i++) {
            service.recordLoginFailure("tenant_001", "admin");
        }
        service.clearLoginFailures("tenant_001", "admin");

        assertThatCode(() -> service.checkLoginAllowed("tenant_001", "admin"))
            .doesNotThrowAnyException();
    }

    @Test
    void shouldLimitCaptchaRequestsByIp() {
        AuthAntiAbuseProperties properties = new AuthAntiAbuseProperties();
        InMemoryAuthAntiAbuseService service = new InMemoryAuthAntiAbuseService(properties);

        for (int i = 0; i < properties.getCaptcha().getMaxRequests(); i++) {
            service.checkCaptchaAllowed("127.0.0.1");
        }

        assertThatThrownBy(() -> service.checkCaptchaAllowed("127.0.0.1"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("429 TOO_MANY_REQUESTS");
    }

    @Test
    void shouldLimitRefreshRequestsByToken() {
        AuthAntiAbuseProperties properties = new AuthAntiAbuseProperties();
        InMemoryAuthAntiAbuseService service = new InMemoryAuthAntiAbuseService(properties);

        for (int i = 0; i < properties.getRefresh().getMaxRequests(); i++) {
            service.checkRefreshAllowed("refresh-token");
        }

        assertThatThrownBy(() -> service.checkRefreshAllowed("refresh-token"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("429 TOO_MANY_REQUESTS");
    }
}
