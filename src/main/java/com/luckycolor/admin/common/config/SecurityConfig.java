package com.luckycolor.admin.common.config;

import com.luckycolor.admin.infrastructure.security.web.JwtAuthenticationFilter;
import com.luckycolor.admin.infrastructure.security.web.JsonAccessDeniedHandler;
import com.luckycolor.admin.infrastructure.security.web.JsonAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;
    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;

    public SecurityConfig(
        JwtAuthenticationFilter jwtAuthenticationFilter,
        JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint,
        JsonAccessDeniedHandler jsonAccessDeniedHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
        this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                .accessDeniedHandler(jsonAccessDeniedHandler))
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    "/auth/captcha",
                    "/api/auth/captcha",
                    "/auth/captcha/challenge",
                    "/api/auth/captcha/challenge",
                    "/auth/captcha/verify",
                    "/api/auth/captcha/verify",
                    "/auth/login",
                    "/api/auth/login",
                    "/auth/refresh",
                    "/api/auth/refresh",
                    "/health",
                    "/api/health",
                    "/version",
                    "/api/version",
                    "/docs/**",
                    "/api/docs/**",
                    "/error",
                    "/api/error",
                    "/swagger-ui/**",
                    "/api/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/api/v3/api-docs/**",
                    "/actuator/health",
                    "/api/actuator/health")
                .permitAll()
                .anyRequest()
                .authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(Customizer.withDefaults())
            .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
