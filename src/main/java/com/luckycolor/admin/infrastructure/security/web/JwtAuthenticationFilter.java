package com.luckycolor.admin.infrastructure.security.web;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAccessTokenClaims;
import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.infrastructure.security.jwt.JwtTokenService;
import com.luckycolor.admin.modules.iam.auth.service.AuthTokenSessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;
    private final AuthTokenSessionService authTokenSessionService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, AuthTokenSessionService authTokenSessionService) {
        this.jwtTokenService = jwtTokenService;
        this.authTokenSessionService = authTokenSessionService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = jwtTokenService.resolveBearerToken(request.getHeader("Authorization"));
            if (token != null) {
                if (authTokenSessionService.isRevoked(token)) {
                    request.setAttribute(SecurityRequestAttributes.AUTH_FAILURE_REASON, "TOKEN_REVOKED");
                    filterChain.doFilter(request, response);
                    return;
                }
                JwtAccessTokenClaims claims;
                try {
                    claims = jwtTokenService.parseAccessToken(token);
                } catch (RuntimeException exception) {
                    request.setAttribute(SecurityRequestAttributes.AUTH_FAILURE_REASON, resolveFailureReason(exception));
                    filterChain.doFilter(request, response);
                    return;
                }
                JwtAuthenticatedUser principal = new JwtAuthenticatedUser(
                    claims.userId(),
                    claims.username(),
                    claims.tenantId(),
                    claims.roles()
                );
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        principal,
                        token,
                        toAuthorities(claims.roles())
                    );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveFailureReason(RuntimeException exception) {
        if (exception instanceof BadCredentialsException) {
            return "TOKEN_INVALID";
        }
        return "TOKEN_INVALID";
    }

    private List<SimpleGrantedAuthority> toAuthorities(List<String> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();
    }
}
