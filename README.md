# luckycolor-admin-springboot

LuckyColor Admin backend rewrite based on Spring Boot.

## Runtime

- Java 17
- Spring Boot 3.5.13
- Maven 3.9+
- MySQL 8.x
- Redis 7.x
- MyBatis-Plus 3.5.x

## Current bootstrap

- Base application scaffold created from Spring Initializr
- Persistence baseline switched to MyBatis-Plus
- Multi-tenant baseline enabled with request header context, tenant SQL isolation, and audit auto-fill
- Context path configured as `/api`
- Swagger UI path configured as `/api/docs`
- Health check endpoint available at `/api/health`
- Migration plan documented in `docs/模块迁移任务拆解.md`

## Start

```powershell
./mvnw spring-boot:run
```
