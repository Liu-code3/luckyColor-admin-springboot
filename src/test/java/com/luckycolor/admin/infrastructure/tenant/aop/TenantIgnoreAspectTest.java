package com.luckycolor.admin.infrastructure.tenant.aop;

import static org.assertj.core.api.Assertions.assertThat;

import com.luckycolor.admin.infrastructure.tenant.annotation.TenantIgnore;
import com.luckycolor.admin.infrastructure.tenant.core.TenantIgnoreContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

class TenantIgnoreAspectTest {

    private final TenantIgnoreAspect aspect = new TenantIgnoreAspect();

    @AfterEach
    void tearDown() {
        TenantIgnoreContextHolder.clear();
    }

    @Test
    void shouldEnableIgnoreScopeForAnnotatedMethod() {
        TestService proxy = createProxy(new TestService());

        boolean ignored = proxy.annotatedMethod();

        assertThat(ignored).isTrue();
        assertThat(TenantIgnoreContextHolder.isIgnoreTenant()).isFalse();
    }

    @Test
    void shouldKeepNormalMethodOutsideIgnoreScope() {
        TestService proxy = createProxy(new TestService());

        boolean ignored = proxy.normalMethod();

        assertThat(ignored).isFalse();
    }

    private TestService createProxy(TestService target) {
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    static class TestService {

        @TenantIgnore
        boolean annotatedMethod() {
            return TenantIgnoreContextHolder.isIgnoreTenant();
        }

        boolean normalMethod() {
            return TenantIgnoreContextHolder.isIgnoreTenant();
        }
    }
}
