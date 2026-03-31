package com.luckycolor.admin.infrastructure.security.datascope;

import com.luckycolor.admin.infrastructure.security.jwt.JwtAuthenticatedUser;
import com.luckycolor.admin.modules.iam.auth.model.AuthUser;
import com.luckycolor.admin.modules.iam.auth.service.AuthUserService;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentDataScopeResolver {

    private static final String SUPER_ADMIN_ROLE = "ROLE_SUPER_ADMIN";

    private final AuthUserService authUserService;

    public CurrentDataScopeResolver(AuthUserService authUserService) {
        this.authUserService = authUserService;
    }

    public Optional<DataScopeRule> resolveCurrentRule() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtAuthenticatedUser principal)) {
            return Optional.empty();
        }
        AuthUser user = authUserService.getByUserId(principal.userId());
        if (user == null) {
            return Optional.empty();
        }
        if (user.roles() != null && user.roles().contains(SUPER_ADMIN_ROLE)) {
            return Optional.of(new DataScopeRule(DataScopeType.ALL, user.tenantId(), user.departmentId(), List.of(), List.of()));
        }

        List<Long> tenantIds = user.scopeTenantIds() == null || user.scopeTenantIds().isEmpty()
            ? (user.tenantId() == null ? List.of() : List.of(user.tenantId()))
            : user.scopeTenantIds();
        List<Long> departmentIds = user.departmentIds() == null || user.departmentIds().isEmpty()
            ? (user.departmentId() == null ? List.of() : List.of(user.departmentId()))
            : user.departmentIds();
        return Optional.of(
            new DataScopeRule(
                DataScopeType.from(user.dataScope()),
                user.tenantId(),
                user.departmentId(),
                tenantIds,
                departmentIds
            )
        );
    }
}
