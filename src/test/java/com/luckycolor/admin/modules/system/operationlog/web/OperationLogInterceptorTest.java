package com.luckycolor.admin.modules.system.operationlog.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogRecordCommand;
import com.luckycolor.admin.modules.system.operationlog.service.OperationLogService;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

class OperationLogInterceptorTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldRecordAdminWriteOperation() throws Exception {
        OperationLogService service = Mockito.mock(OperationLogService.class);
        OperationLogInterceptor interceptor = new OperationLogInterceptor(service);
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), controllerMethod("create"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/users");
        request.setContextPath("/api");
        request.setRemoteAddr("127.0.0.1");
        request.addParameter("username", "admin");
        request.addParameter("password", "secret123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                new JwtAuthenticatedUser(1L, "admin", 1L, List.of("ROLE_SUPER_ADMIN")),
                "token",
                List.of()
            )
        );

        interceptor.preHandle(request, response, handlerMethod);
        interceptor.afterCompletion(request, response, handlerMethod, null);

        ArgumentCaptor<OperationLogRecordCommand> captor = ArgumentCaptor.forClass(OperationLogRecordCommand.class);
        verify(service).record(captor.capture());
        OperationLogRecordCommand command = captor.getValue();
        assertThat(command.bizModule()).isEqualTo("users");
        assertThat(command.operationType()).isEqualTo("CREATE");
        assertThat(command.username()).isEqualTo("admin");
        assertThat(command.requestParams()).contains("username=admin");
        assertThat(command.requestParams()).contains("password=******");
        assertThat(command.success()).isEqualTo(1);
    }

    @Test
    void shouldSkipReadOperation() throws Exception {
        OperationLogService service = Mockito.mock(OperationLogService.class);
        OperationLogInterceptor interceptor = new OperationLogInterceptor(service);
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), controllerMethod("page"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users/page");
        request.setContextPath("/api");
        MockHttpServletResponse response = new MockHttpServletResponse();

        interceptor.preHandle(request, response, handlerMethod);
        interceptor.afterCompletion(request, response, handlerMethod, null);

        verify(service, never()).record(any());
    }

    private Method controllerMethod(String methodName) throws NoSuchMethodException {
        return TestController.class.getDeclaredMethod(methodName);
    }

    @SuppressWarnings("unused")
    private static final class TestController {

        public void create() {
        }

        public void page() {
        }
    }
}
