# luckycolor-admin-springboot

LuckyColor Admin backend rewrite based on Spring Boot.

## Runtime

- Java 17
- Spring Boot 3.5.13
- Maven 3.9+
- MySQL 8.x
- Redis 7.x
- MyBatis-Plus 3.5.x
- Flyway

## Current bootstrap

- Base application scaffold created from Spring Initializr
- Persistence baseline switched to MyBatis-Plus
- Multi-tenant baseline enabled with request header context, tenant SQL isolation, and audit auto-fill
- Redis cache baseline added with JSON serialization and unified key prefix
- Login captcha baseline added with SVG image generation and Redis-backed one-time verification
- Local login baseline added with captcha validation, password checking, JWT issuing, and audit logging
- Data scope baseline added for tenant-range filtering on platform queries
- Security audit baseline added for login outcomes, unauthorized attempts, access denied events, and audit paging
- Use `@TenantIgnore` on public-data flows when a method must bypass tenant SQL injection explicitly
- Mapper interfaces should use `@Mapper` explicitly instead of wide-package scanning
- Shared paging contracts are available via `PageQuery`, `PageResult`, and `BaseMapperX`
- Tenant resolution now supports `x-tenant-id` header first and `Bearer Token` tenant claim second
- Flyway baseline script added under `src/main/resources/db/migration`
- Context path configured as `/api`
- Swagger UI path configured as `/api/docs`
- Health check endpoint available at `/api/health`
- Migration plan documented in `docs/模块迁移任务拆解.md`

## Start

```powershell
./mvnw spring-boot:run
```

Enable Flyway migration when needed:

```powershell
$env:FLYWAY_ENABLED="true"
./mvnw spring-boot:run
```

Default cache key prefix:

```text
luckycolor-admin:<namespace>:<segment...>
```

Login captcha endpoint:

```text
GET /api/auth/captcha
```

Login endpoint:

```text
POST /api/auth/login
```

Current user endpoints:

```text
POST /api/auth/logout
GET /api/auth/profile
GET /api/auth/permissions
GET /api/auth/routes
GET /api/auth/access
```

Security audit endpoint:

```text
GET /api/admin/security-audit-logs/page
```
