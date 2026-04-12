# ECC 全功能联调与业务走通测试手册

## 1. 文档目标

- 面向当前 Spring Boot 后端仓库 `D:\zl\luckycolor-admin-springboot`
- 配套前端仓库 `D:\zl\luckyColor-admin`
- 用 ECC 的思路，把“能不能跑起来”拆成可执行、可留痕、可回归的验证闭环
- 输出一套覆盖认证、租户、系统管理、平台能力、前端动态路由与接口契约的全功能测试手册

本文档不追求一次性把所有测试都塞进一个命令，而是强调：

1. 先研究现状，再执行验证
2. 先自动化基线，再人工业务走通
3. 先关键主链路，再外围能力
4. 每轮验证都保留证据，失败后能快速回归

## 2. ECC 思想在本项目里的落地方式

### 2.1 研究优先

- 先看当前仓库已有模块、接口、前端页面、测试用例、文档，再设计测试顺序
- 测试清单优先复用现有控制器测试、服务测试、回归测试与前端 Playwright 冒烟用例

### 2.2 分层验证

按 ECC 的验证闭环，当前项目建议分成 5 层：

1. 环境与启动基线
2. 后端自动化回归
3. 接口契约与权限边界检查
4. 前端自动化冒烟
5. 人工全链路业务走通

### 2.3 小步留痕

每个阶段都要至少保留一种证据：

- 终端执行结果
- Swagger 响应截图
- 前端页面截图
- 数据库查询结果
- Redis key / 值验证结果
- 日志片段

### 2.4 故障隔离

出现失败时按顺序定位：

1. 环境问题：MySQL / Redis / 端口 / 配置
2. 启动问题：Flyway / Spring Boot / 前端代理
3. 契约问题：字段、路径、权限、租户头
4. 前端渲染问题：路由恢复、菜单树、按钮权限、页面报错
5. 数据问题：种子数据、缓存、角色菜单关系

## 3. 测试对象与联调矩阵

| 对象 | 路径 | 默认地址 | 说明 |
| --- | --- | --- | --- |
| Spring Boot 后端 | `D:\zl\luckycolor-admin-springboot` | `http://127.0.0.1:3001/api` | 当前主测试对象 |
| 前端管理台 | `D:\zl\luckyColor-admin` | `http://127.0.0.1:9900` | 通过 Vite 代理转发 `/api` |
| Redis | 本地实例 | `127.0.0.1:6379/0` | 验证验证码、缓存、会话相关能力 |
| MySQL | 本地实例 | `127.0.0.1:3306/luckycolor_admin_sb` | 验证迁移、种子数据、业务落库 |

默认账号：

- 用户名：`admin`
- 密码：`123456`
- 默认租户请求头：`x-tenant-id: 1`

## 4. 测试前准备

### 4.1 依赖要求

- JDK 17+
- Maven Wrapper 可用
- Node.js 18+
- pnpm 8+
- MySQL 8+
- Redis 7+

### 4.2 后端配置基线

确认 `src/main/resources/application.yml` 中的本地默认配置可用：

- 端口：`3001`
- 上下文：`/api`
- 数据库：`luckycolor_admin_sb`
- Redis：`127.0.0.1:6379/0`
- Flyway：默认开启

### 4.3 前端配置基线

在 `D:\zl\luckyColor-admin` 中确认：

- `pnpm dev` 默认等价于 `pnpm dev:springboot`
- 前端访问地址为 `http://127.0.0.1:9900`
- 前端通过 `/api` 代理到 Spring Boot 后端

### 4.4 Playwright 额外说明

前端仓库已有 `pnpm test:smoke`，但其 `tests/playwright/helpers/admin.ts` 默认会从 `../luckyColor-admin-serve` 加载 `ioredis` 包。

因此自动化冒烟前建议满足以下任一条件：

1. 保持 `D:\zl\luckyColor-admin-serve` 已安装依赖
2. 或者自行设置 `PLAYWRIGHT_BACKEND_ROOT` 到一个已安装 `ioredis` 的目录

## 5. 推荐执行顺序

### Gate 0：环境与启动就绪

先启动后端：

```powershell
.\mvnw.cmd spring-boot:run
```

再启动前端：

```powershell
pnpm install
pnpm dev:springboot
```

检查以下地址：

- 前端首页：`http://127.0.0.1:9900`
- 后端健康检查：`http://127.0.0.1:3001/api/health`
- Swagger：`http://127.0.0.1:3001/api/docs`
- OpenAPI：`http://127.0.0.1:3001/api/v3/api-docs`
- 版本信息：`http://127.0.0.1:3001/api/version`

放行标准：

- Spring Boot 启动无异常退出
- Flyway 迁移完成
- 前端首页可打开
- Swagger 页面可加载

### Gate 1：后端自动化基线

先跑全量：

```powershell
.\mvnw.cmd test
```

如果需要按模块分批执行，建议按下面顺序跑。

#### 5.1 认证、安全、租户基线

```powershell
.\mvnw.cmd -Dtest=AuthControllerTest,AuthServiceImplTest,LoginCaptchaServiceImplTest,JwtAuthenticationFilterTest,PermissionGuardIntegrationTest,TenantContextFilterTest,TenantContextFilterMissingTenantTest,FileAccessSecurityIntegrationTest,SecurityAuditLogControllerTest test
```

覆盖重点：

- 验证码生成、登录、退出、资料、权限、路由
- JWT 过滤器、鉴权拒绝、租户头校验
- 安全审计与文件访问边界

#### 5.2 租户中心

```powershell
.\mvnw.cmd -Dtest=TenantPackageControllerTest,TenantPackageServiceImplTest,TenantControllerTest,TenantServiceImplTest,TenantBootstrapControllerTest,TenantBootstrapControllerSecurityIntegrationTest,TenantBootstrapServiceImplTest,TenantAuditLogControllerTest,TenantAuditLogServiceImplTest,TenantBootstrapPermissionMigrationTest test
```

覆盖重点：

- 租户套餐、租户管理、租户初始化、租户审计
- 初始化权限迁移与租户安全边界

#### 5.3 系统管理

```powershell
.\mvnw.cmd -Dtest=SystemUserControllerTest,SystemUserServiceImplTest,SystemRoleControllerTest,SystemRoleServiceImplTest,SystemDepartmentControllerTest,SystemDepartmentServiceImplTest,MenuControllerTest,MenuServiceImplTest,DictionaryTypeControllerTest,DictionaryTypeServiceImplTest,DictionaryItemControllerTest,DictionaryItemServiceImplTest,DictionaryCatalogControllerTest,SystemConfigControllerTest,SystemConfigServiceImplTest,NoticeControllerTest,NoticeServiceImplTest,OperationLogControllerTest,OperationLogServiceImplTest test
```

覆盖重点：

- 用户、角色、部门、菜单、字典、配置、公告、操作日志

#### 5.4 平台能力与契约

```powershell
.\mvnw.cmd -Dtest=DashboardControllerTest,DashboardServiceImplTest,StorageControllerTest,FileStorageServiceImplTest,I18nResourceControllerTest,I18nResourceServiceImplTest,UserPreferenceControllerTest,UserPreferenceServiceImplTest,WatermarkConfigControllerTest,WatermarkConfigServiceImplTest,CodegenMetadataControllerTest,CodegenMetadataServiceImplTest,ApiDocsControllerTest,ApiDocsHttpIntegrationTest,OpenApiDocsListingTest,OpenApiResponseContractTest,HealthControllerTest,VersionControllerTest,CoreRegressionIntegrationTest,FrontendCompatibilityControllerTest,FrontendSystemCompatibilityControllerTest,FrontendContentCompatibilityControllerTest,FrontendTenantCompatibilityControllerTest test
```

覆盖重点：

- 工作台、文件、国际化、偏好、水印、代码生成、文档、健康检查
- 前端兼容接口与核心回归链路

放行标准：

- 全量或分批测试均通过
- 失败时能定位到具体模块，不允许带着未知失败进入前端联调

### Gate 2：接口契约与权限边界检查

建议使用 Swagger 或 Apifox 按下面顺序核查。

#### 5.5 登录与会话

- `GET /api/auth/captcha`
- `GET /api/auth/captcha/challenge`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `GET /api/auth/profile`
- `GET /api/auth/permissions`

检查点：

- 验证码可生成，Redis 中能看到挑战数据
- 登录成功后返回 token
- `profile` 与 `permissions` 能读取当前登录人信息
- 错误密码、错误验证码、缺租户头场景返回合理错误

#### 5.6 路由、访问、菜单契约

- `GET /api/auth/routes`
- `GET /api/auth/access`
- `GET /api/menus`
- `GET /api/menus/tree`
- `POST /api/menus`
- `PUT /api/menus/sync`
- `PATCH /api/menus/{id}`
- `PATCH /api/menus/{id}/status`
- `DELETE /api/menus/{id}`

检查点：

- 与 `docs/menu-contract-alignment-checklist.md` 中的契约要求一致
- 页面切换菜单后动态路由可恢复
- 菜单显隐、排序、类型、权限码、meta 信息符合前端期望

#### 5.7 租户与权限边界

- 带 `x-tenant-id: 1` 请求时查询正常
- 缺少租户头时，仅白名单接口可访问
- 切换到非授权租户头时，被拒绝或数据隔离正确
- 使用无权限账号访问管理接口时，返回 401/403 且前端不崩溃

### Gate 3：前端自动化冒烟

在 `D:\zl\luckyColor-admin` 下执行：

```powershell
pnpm install
pnpm test:smoke
```

如需带界面观察：

```powershell
pnpm test:smoke:headed
```

当前已存在的前端冒烟覆盖了以下重点：

- 登录页渲染
- 未登录访问首页跳转登录
- 已登录恢复上次访问页面
- 系统管理主链路页面打开与弹窗展示
- 租户管理、租户套餐页面打开与主要操作入口展示
- 菜单可见性、iframe 菜单、外链菜单、404 回退、keep-alive、白名单路由

放行标准：

- 页面无白屏
- 控制台无报错
- 网络无 4xx / 5xx 未处理错误
- 登录、菜单加载、主要列表页和弹窗都可打开

### Gate 4：人工全链路业务走通

这一层是 ECC 最强调的“真实业务流闭环”。自动化通过后，仍然要人工跑完整链路。

## 6. 人工业务走通清单

### 6.1 主链路 A：登录到工作台

步骤：

1. 打开前端登录页
2. 使用 `admin / 123456` 登录
3. 验证登录后默认进入工作台或上次访问页
4. 刷新页面，验证登录态仍有效
5. 退出登录，验证重新回到登录页

预期：

- 登录成功
- 顶部用户信息正确
- 左侧菜单树正确渲染
- 刷新后动态路由不丢失
- 退出后无法继续访问受保护页面

### 6.2 主链路 B：租户套餐 -> 租户 -> 初始化

前端入口：

- `租户中心 -> 租户套餐`
- `租户中心 -> 租户管理`

步骤：

1. 新增一个租户套餐
2. 配置菜单范围 / 能力开关
3. 新增一个租户，关联套餐
4. 执行租户初始化
5. 检查初始化结果

预期：

- 套餐可新增、编辑、启停
- 租户可新增、启停、删除前确认
- 初始化后生成管理员、角色、菜单等基础资源
- 租户审计日志可查询到关键操作

建议留痕：

- 新租户 ID
- 初始化返回结果
- 数据库中租户、角色、用户、菜单记录截图

### 6.3 主链路 C：角色、菜单、用户授权

前端入口：

- `系统管理 -> 菜单管理`
- `系统管理 -> 角色管理`
- `系统管理 -> 用户管理`

步骤：

1. 新增菜单或调整现有菜单状态
2. 新增角色并分配菜单
3. 新增用户并分配角色
4. 用该用户重新登录
5. 检查可见菜单、按钮权限、接口权限

预期：

- 角色菜单变更后，`/api/auth/routes` 和 `/api/auth/access` 输出同步变化
- 新用户登录后只能看到被授权菜单
- 未授权按钮不可见或不可操作

### 6.4 主链路 D：部门、字典、配置、公告

前端入口：

- `系统管理 -> 部门管理`
- `系统管理 -> 字典管理`
- `系统管理 -> 系统配置`
- `系统管理 -> 通知公告`

步骤：

1. 新增部门，修改部门信息，再删除或禁用
2. 新增字典类型与字典项，刷新缓存
3. 新增系统配置，切换状态，刷新缓存
4. 新增公告，预览，发布，查看列表状态

预期：

- 树形和列表数据都能刷新
- 缓存刷新后查询结果正确
- 发布前后公告状态变化正确
- 前端操作提示与后端实际状态一致

### 6.5 主链路 E：平台能力

建议从左侧菜单逐项验证以下模块：

| 模块 | 关键动作 | 预期 |
| --- | --- | --- |
| 工作台 | 打开首页、刷新页面、切换标签页 | 概览卡片正常、访问记录不报错 |
| 文件服务 | 上传文件、访问下载地址、删除文件 | 路径安全校验正确，匿名访问边界正确 |
| 国际化资源 | 查询资源、新增或修改资源、启停 | 列表与详情同步更新 |
| 用户偏好 | 修改主题/布局偏好并保存 | 刷新页面后偏好保留 |
| 水印配置 | 读取当前配置、修改并保存 | 新配置刷新后生效 |
| 代码生成 | 表发现、导入、分页、详情、字段维护 | 元数据读写正常，无接口错误 |

### 6.6 主链路 F：日志与审计

前端入口：

- `系统管理 -> 系统日志`
- 安全审计相关入口或 Swagger 接口

步骤：

1. 先做一次成功登录、一次失败登录
2. 再执行一次无权限访问
3. 再执行一次关键写操作，例如新增配置或公告
4. 查询操作日志与安全审计日志

预期：

- 登录成功 / 失败均有审计记录
- 未授权访问有安全事件
- 写操作有操作日志
- 日志查询条件、分页、时间范围正常

## 7. 前端页面与后端模块映射

| 前端页面/菜单 | 后端控制器或能力 |
| --- | --- |
| 登录页 | `AuthController` |
| 工作台 | `DashboardController` |
| 用户管理 | `SystemUserController` |
| 角色管理 | `SystemRoleController` |
| 部门管理 | `SystemDepartmentController` |
| 菜单管理 | `MenuController` |
| 字典管理 | `DictionaryTypeController`、`DictionaryItemController`、`DictionaryCatalogController` |
| 系统配置 | `SystemConfigController` |
| 通知公告 | `NoticeController` |
| 操作日志 | `OperationLogController` |
| 安全审计 | `SecurityAuditLogController` |
| 租户套餐 | `TenantPackageController` |
| 租户管理 | `TenantController` |
| 租户初始化 | `TenantBootstrapController` |
| 租户审计 | `TenantAuditLogController` |
| 文件服务 | `StorageController` |
| 国际化 | `I18nResourceController` |
| 用户偏好 | `UserPreferenceController` |
| 水印配置 | `WatermarkConfigController` |
| 代码生成 | `CodegenMetadataController` |
| 健康检查 / 版本 | `HealthController`、`VersionController` |

## 8. 当前自动化覆盖缺口

基于现有仓库状态，建议关注以下缺口：

- 前端 Playwright 冒烟已经覆盖登录、系统管理、租户管理和路由恢复，但对平台能力模块的真实写操作覆盖还不够
- 文件上传下载、代码生成、国际化、水印、用户偏好更适合补充至少一轮人工完整操作
- 多租户跨账号、跨租户、权限降级场景建议增加一轮专门的人工回归
- 如果准备发布或大规模重构，建议再补一组真正从前端发起的 E2E 测试，覆盖“创建角色 -> 创建用户 -> 登录新用户 -> 验证菜单权限”的完整链路

## 9. 失败时的回滚与定位建议

### 9.1 登录失败

- 先查 Redis 中验证码 challenge 是否生成
- 再查 `/api/auth/login` 返回体
- 再查 `AuthControllerTest`、`AuthServiceImplTest`

### 9.2 页面有菜单但打不开

- 先查 `/api/auth/routes`
- 再查前端 `AUTH_MENU_TREE` 缓存
- 再查 `docs/menu-contract-alignment-checklist.md`

### 9.3 有按钮但提交失败

- 先查按钮权限码与角色菜单关系
- 再查实际调用接口是否需要额外权限或租户头
- 再查对应控制器测试

### 9.4 新增成功但列表不刷新

- 先查接口返回体结构
- 再查前端列表刷新逻辑
- 再查缓存是否需要手动刷新

## 10. 最终放行标准

满足以下条件才算“业务逻辑走通、系统可正常运行”：

1. Spring Boot 可稳定启动，健康检查、Swagger、版本接口可访问
2. 后端自动化测试通过，至少核心回归、认证安全、租户中心、系统管理、平台能力四组通过
3. 前端 `pnpm test:smoke` 通过，页面无明显渲染和路由错误
4. 管理员从登录到工作台、租户、系统管理、平台能力至少各完成一轮真实操作
5. 菜单、权限、租户、缓存、日志、审计链路都有证据留存
6. 遇到失败项已经定位并回归验证，而不是仅靠人工忽略

## 11. 推荐执行记录模板

每轮联调建议记录以下信息：

```text
测试时间：
测试分支：
后端提交：
前端提交：
数据库：
Redis：

Gate 0：通过 / 失败
Gate 1：通过 / 失败
Gate 2：通过 / 失败
Gate 3：通过 / 失败
Gate 4：通过 / 失败

失败项：
定位结论：
回归结果：
```

## 12. 本文档对应的当前仓库依据

- 后端模块拆解：`docs/模块迁移任务拆解.md`
- 菜单契约检查：`docs/menu-contract-alignment-checklist.md`
- 后端 README：`README.md`
- 前端 README：`D:\zl\luckyColor-admin\README.md`
- 前端 Playwright 冒烟：`D:\zl\luckyColor-admin\tests\playwright\smoke\*.spec.ts`

如果后续新增模块、前端菜单或权限模型发生变化，必须同步更新本文档，否则测试清单会逐步失真。
