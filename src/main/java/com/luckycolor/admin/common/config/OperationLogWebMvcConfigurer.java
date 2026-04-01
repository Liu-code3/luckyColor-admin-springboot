package com.luckycolor.admin.common.config;

import com.luckycolor.admin.modules.system.operationlog.web.OperationLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class OperationLogWebMvcConfigurer implements WebMvcConfigurer {

    private final ObjectProvider<OperationLogInterceptor> operationLogInterceptorProvider;

    public OperationLogWebMvcConfigurer(ObjectProvider<OperationLogInterceptor> operationLogInterceptorProvider) {
        this.operationLogInterceptorProvider = operationLogInterceptorProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        operationLogInterceptorProvider.ifAvailable(interceptor ->
            registry.addInterceptor(interceptor).addPathPatterns("/admin/**")
        );
    }
}
