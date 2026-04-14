# luckycolor-admin-springboot

<div align="center">
  <h2>LuckyColor Admin Spring Boot</h2>
  <p>⚡️ 基于 Spring Boot 3 + MyBatis-Plus + MySQL + Redis + SpringDoc 的轻量级多租户后台管理后端模板</p>
  <p>
    <img alt="Java" src="https://img.shields.io/badge/Java-17-007396.svg" />
    <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5.13-6DB33F.svg" />
    <img alt="MyBatis Plus" src="https://img.shields.io/badge/MyBatis--Plus-3.5.x-1F8ACB.svg" />
    <img alt="MySQL" src="https://img.shields.io/badge/MySQL-8.x-4479A1.svg" />
    <img alt="Redis" src="https://img.shields.io/badge/Redis-7.x-DC382D.svg" />
  </p>
</div>

<p align="center">
  面向中后台与 SaaS 场景，内置认证鉴权、RBAC 权限、动态路由、多租户隔离、系统管理与平台通用能力。
</p>

<p align="center">
  <a href="#项目简介">项目简介</a>
  <span>&nbsp;|&nbsp;</span>
  <a href="#仓库与配套项目">仓库与配套项目</a>
  <span>&nbsp;|&nbsp;</span>
  <a href="#快速开始">快速开始</a>
  <span>&nbsp;|&nbsp;</span>
  <a href="#功能概览">功能概览</a>
  <span>&nbsp;|&nbsp;</span>
  <a href="#项目结构">项目结构</a>
  <span>&nbsp;|&nbsp;</span>
  <a href="#项目文档">项目文档</a>
</p>

<p align="center">
  <a href="https://github.com/Liu-code3/luckyColor-admin">前端仓库</a>
  <span>&nbsp;|&nbsp;</span>
  <a href="https://github.com/Liu-code3/luckyColor-admin-serve">NestJS 后端</a>
  <span>&nbsp;|&nbsp;</span>
  <a href="http://127.0.0.1:3001/api/docs">本地 Swagger</a>
</p>

## 项目速览

- 技术定位：`Spring Boot 3 + Java 17` 的后台管理后端模板，适合多租户 SaaS、中后台和运营平台项目快速起步
- 内置能力：认证登录、JWT、验证码、RBAC 权限、动态路由、租户隔离、字典配置、通知公告、平台通用模块
- 工程能力：集成 `MyBatis-Plus`、`Redis`、`Spring Security`、`SpringDoc`，保留初始化 SQL、种子数据和回归测试
- 配套生态：可与 `luckyColor-admin` 前端、`luckyColor-admin-serve` NestJS 后端进行对照联调和契约对齐

## 项目简介

`luckycolor-admin-springboot` 是 LuckyColor 后台管理系统的 Spring Boot 后端实现，基于 `Spring Boot 3.5 + Java 17 + MyBatis-Plus + MySQL + Redis` 构建，重点关注前端契约稳定、多租户边界、权限控制与可持续演进的工程结构。

当前仓库已经具备以下基础：

- 统一的登录认证、JWT 登录态、权限聚合、动态路由与按钮权限输出
- 多租户上下文识别、租户隔离、租户管理、租户套餐与租户初始化能力
- 用户、角色、菜单、部门、字典、系统配置、通知公告等后台常用模块
- 文件服务、国际化资源、用户偏好、水印配置、代码生成器等平台能力
- 初始化 SQL、种子数据、OpenAPI 文档、回归测试与基础工程化脚本

适合作为多租户管理后台、运营后台、企业内部管理系统或中后台脚手架的 Java 后端基础工程。

## 仓库与配套项目

| 项目 | 类型 | 地址 |
| --- | --- | --- |
| `luckyColor-admin` | 前端管理台 | https://github.com/Liu-code3/luckyColor-admin |
| `luckyColor-admin-serve` | NestJS 后端 | https://github.com/Liu-code3/luckyColor-admin-serve |
| `luckycolor-admin-springboot` | Spring Boot 后端 | 当前仓库 |

前端本地联调时可以在两个后端实现之间切换代理目标，用于对齐接口契约与页面行为。

## 技术栈

| 类别 | 说明 |
| --- | --- |
| 后端框架 | Spring Boot 3.5.13 |
| 开发语言 | Java 17 |
| 安全框架 | Spring Security |
| ORM / 持久层 | MyBatis-Plus 3.5.x |
| 数据库 | MySQL 8.x |
| 缓存 | Redis 7.x |
| 数据初始化 | SQL 脚本 |
| API 文档 | springdoc OpenAPI |
| 构建工具 | Maven Wrapper |
| 测试 | JUnit 5 / Spring Boot Test |

## 快速开始

### 1. 环境要求

| 组件 | 版本要求 |
| --- | --- |
| JDK | 17+ |
| Maven | 3.9+ 或使用仓库自带 Wrapper |
| MySQL | 8.x |
| Redis | 7.x |

### 2. 准备数据库与 Redis

默认本地配置如下：

- MySQL Host：`127.0.0.1`
- MySQL Port：`3306`
- Database：`luckycolor_admin_sb`
- Username：`root`
- Password：`123456`
- Redis Host：`127.0.0.1`
- Redis Port：`6379`
- Redis Database：`0`

### 3. 配置环境变量

当前项目默认从 `src/main/resources/application.yml` 读取本地开发配置，也支持通过环境变量覆盖。

常用环境变量示例：

```env
SERVER_PORT=3001
DB_HOST=127.0.0.1
DB_PORT=3306
DB_NAME=luckycolor_admin_sb
DB_USERNAME=root
DB_PASSWORD=123456
REDIS_HOST=127.0.0.1
REDIS_PORT=6379
REDIS_DATABASE=0
JWT_SECRET=replace-with-a-strong-secret-for-luckycolor-admin
TENANT_ENABLED=true
TENANT_HEADER=x-tenant-id
DEFAULT_TENANT_ID=
```

### 4. 启动项目

```powershell
.\mvnw.cmd spring-boot:run
```

启动成功后可访问：

- API 基础路径：`http://127.0.0.1:3001/api`
- Swagger 文档：`http://127.0.0.1:3001/api/docs`
- OpenAPI JSON：`http://127.0.0.1:3001/api/v3/api-docs`
- 健康检查：`GET http://127.0.0.1:3001/api/health`

### 5. 打包项目

```powershell
.\mvnw.cmd clean package
```

如需使用生产配置启动：

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
.\mvnw.cmd spring-boot:run
```

## 默认初始化数据

当前仓库使用 MyBatis-Plus 作为持久层。联调环境需要提前准备可用的数据库结构与基础数据，至少应包含：

- 默认租户与租户套餐
- 内置管理员、租户运营账号与基础角色
- 系统菜单树与权限点
- 部门、字典、系统配置、公告等基础数据
- 与当前前端契约对齐的本地化引导数据

默认本地管理员账号：

- 用户名：`admin`
- 密码：`123456`

## 多租户机制

当前项目已接入统一的租户上下文识别，优先级如下：

1. 请求头 `x-tenant-id`
2. `Bearer Token` 中的租户信息

默认租户相关能力已覆盖请求头解析、SQL 隔离、上下文透传与权限边界控制。

联调阶段可优先使用请求头：

```http
x-tenant-id: 1
```

## 功能概览

### 认证与权限

- 登录、登出、当前用户信息
- SVG 验证码与登录校验
- JWT 登录态与刷新链路
- 权限聚合输出
- 前端动态路由树输出
- 按钮权限码与安全审计能力

### 系统管理

- 用户管理
- 角色管理
- 菜单管理
- 部门管理
- 字典类型与字典项管理
- 系统配置
- 通知公告

### 租户中心

- 租户管理
- 租户套餐管理
- 租户初始化与基础资源装配

### 平台能力

- 文件服务
- 国际化资源
- 用户偏好
- 水印配置
- 代码生成器元数据管理
- 健康检查与版本信息输出

## 项目结构

```text
luckycolor-admin-springboot/
├─ src/
│  ├─ main/
│  │  ├─ java/com/luckycolor/admin/
│  │  │  ├─ common/                 # 通用响应、配置、异常、基础契约
│  │  │  ├─ infrastructure/         # 安全、租户、缓存、持久层基础设施
│  │  │  └─ modules/                # iam、system、tenant、platform 等业务模块
│  │  └─ resources/
│  │     ├─ application.yml         # 默认运行配置
│  │     └─ application-prod.yml    # 生产环境配置
│  └─ test/java/com/luckycolor/admin/ # 控制器、服务、安全、回归测试
├─ docs/                            # 项目文档与协作说明
├─ scripts/git/                     # Git 提交辅助脚本
├─ mvnw / mvnw.cmd                  # Maven Wrapper
└─ pom.xml                          # 依赖与构建配置
```

## 常用命令

| 命令 | 说明 |
| --- | --- |
| `.\mvnw.cmd spring-boot:run` | 启动开发环境 |
| `.\mvnw.cmd test` | 运行全部测试 |
| `.\mvnw.cmd -Dtest=类名 test` | 运行指定测试类 |
| `.\mvnw.cmd clean package` | 构建可运行产物 |
| `.\mvnw.cmd clean verify` | 执行完整校验与构建 |

如需安装本地提交规范钩子：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\git\install-gitflow-hooks.ps1
```

## 前端联调

配套前端仓库：`https://github.com/Liu-code3/luckyColor-admin`

前端本地可通过脚本切换代理目标：

- `pnpm dev:springboot`
- `pnpm dev:nestjs`

本地联调地址约定：

- Spring Boot 后端：`http://127.0.0.1:3001/api`
- NestJS 后端：`http://127.0.0.1:3002/api`

## 测试与校验

当前仓库已包含基础测试与回归检查：

- 控制器测试：核心接口出参与权限入口校验
- 安全测试：认证、租户上下文、权限守卫与文件访问路径
- 回归测试：关键契约与迁移链路回归

推荐提交前至少执行一次：

```powershell
.\mvnw.cmd test
```

## 项目文档

`docs/` 目录已经沉淀了当前阶段的协作文档，建议按场景查阅：

| 文档 | 说明 |
| --- | --- |
| `docs/模块迁移任务拆解.md` | 模块迁移进度与实施拆解 |
| `docs/menu-contract-alignment-checklist.md` | 菜单与前端契约对齐检查项 |
| `docs/安全与架构优化执行计划.md` | 安全与架构优化计划 |
| `docs/release/v1.0.0-alpha.1.md` | 当前版本发布说明 |
| `docs/release/v1.0.0-alpha.1-checklist.md` | 当前版本发布检查清单 |
| `LUCKYCOLOR_前后端联调与契约检查工作流.md` | 前后端联调与契约检查工作流 |

## 发布状态

当前版本：`1.0.0-alpha.1`

当前项目仍处于持续演进阶段，目标是在保持前端契约稳定的前提下，逐步补齐多租户、权限、安全、平台能力与工程质量。

## License

当前仓库尚未附带明确的开源许可证文件。

如果你准备将该项目正式作为开源项目公开发布，建议补充 `LICENSE` 文件，明确代码的使用、修改与分发边界。
