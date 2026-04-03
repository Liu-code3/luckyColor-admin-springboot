# Spring Boot 菜单接口对齐改造清单

## 1. 目标

- Spring Boot 作为 NestJS 的可替换接口服务存在。
- 前端切换 `D:/zl/luckyColor-admin-serve` 或 `D:/zl/luckycolor-admin-springboot` 时，菜单、权限、路由相关接口应保持一致的地址、字段语义和过滤行为。
- 菜单英文/中文不一致的问题，按“契约不一致”处理，不按单点文案问题处理。

## 2. 当前结论

### 2.1 已对齐项

- 两边 API 前缀都是 `/api`
- 两边默认都连接 `luckycolor_admin`
- 两边默认都连接本地 Redis

### 2.2 未对齐项

- `/api/auth/routes` 数据来源不同
- `/api/auth/routes` 返回结构不同
- `/api/auth/access` 返回结构不同
- `/api/menus/tree` 查询语义不同
- `/api/menus` 和 `/api/menus/tree` 字段模型不同
- Spring Boot 缺少 `/api/menus/sync`
- Spring Boot 缺少兼容版 `PATCH /api/menus/{id}/status`
- Spring Boot 当前没有等价 `menuKey`
- Spring Boot 当前把菜单权限和按钮权限混在一套权限编码里

## 3. NestJS 真实契约

### 3.1 菜单管理

- `GET /api/menus`
- `GET /api/menus/tree`
- `GET /api/menus/{id}`
- `POST /api/menus`
- `PUT /api/menus/sync`
- `PATCH /api/menus/{id}`
- `PATCH /api/menus/{id}/status`
- `DELETE /api/menus/{id}`

### 3.2 认证菜单

- `GET /api/auth/routes`
- `GET /api/auth/access`

### 3.3 NestJS 菜单模型

NestJS `/api/menus` 和 `/api/menus/tree` 的菜单对象核心字段：

- `pid`
- `id`
- `title`
- `name`
- `type`
- `path`
- `key`
- `permissionCode`
- `icon`
- `layout`
- `isVisible`
- `status`
- `component`
- `redirect`
- `meta`
- `sort`
- `createdAt`
- `updatedAt`

NestJS `/api/auth/routes` 的路由对象核心字段：

- `path`
- `name`
- `component`
- `redirect`
- `meta`
- `children`

NestJS `/api/auth/access` 的核心结构：

- `user`
- `roles`
- `menuTree`

## 4. Spring Boot 当前偏差

### 4.1 `/api/auth/routes`

当前实现：

- 来源于 `application.yml -> app.security.access.routes`
- 不是基于数据库角色菜单生成
- 返回结构是 `code/fullPath/icon/hidden/alwaysShow/keepAlive/permissions`

目标实现：

- 来源于数据库中的可访问菜单
- 返回结构改为 NestJS 风格的路由树
- `meta` 内需要输出 `title/icon/hidden/order/menuKey/permissionCode/type/layout`

### 4.2 `/api/auth/access`

当前实现：

- 只返回 `userId/tenantId/roles/permissions/routeCodes/homePath`

目标实现：

- 改为返回 `user + roles + menuTree`
- `user` 下需要输出：
  - `id`
  - `tenantId`
  - `username`
  - `nickname`
  - `roleCodes`
  - `menuCodeList`
  - `buttonCodeList`

### 4.3 `/api/menus/tree`

当前实现：

- 仅返回全量菜单树
- 不支持 `view`
- 不支持 `roleId`
- 会把路径和组件适配成旧后台写法

目标实现：

- 支持 `view=platform|tenant`
- 支持 `roleId`
- 返回数据库语义下的菜单树
- 不再对 `path/component` 做硬编码改写

### 4.4 `/api/menus`

当前实现：

- 返回了兼容字段 `parentId/tenantId/tenantName/menuKey`
- 未显式返回 NestJS 的 `permissionCode`

目标实现：

- 以 NestJS 菜单模型为准
- 保证 `permissionCode` 存在
- 非必要兼容字段不参与对齐契约定义

### 4.5 菜单写接口

当前实现：

- 有 `POST /api/menus`
- 有 `PATCH /api/menus/{id}`
- 无 `PUT /api/menus/sync`
- 无兼容版 `PATCH /api/menus/{id}/status`

目标实现：

- 补齐 `PUT /api/menus/sync`
- 补齐 `PATCH /api/menus/{id}/status`
- 写接口返回值与 NestJS 对齐

## 5. 结构性阻塞点

### 5.1 表结构不等价

NestJS 菜单表是 `menus`，核心字段包括：

- `title`
- `name`
- `type`
- `path`
- `menu_key`
- `permission_code`
- `layout`
- `is_visible`
- `component`
- `redirect`
- `meta`

Spring Boot 当前菜单表是 `sys_menu`，核心字段包括：

- `menu_name`
- `menu_type`
- `route_name`
- `route_path`
- `permission_code`
- `icon`
- `visible`
- `keep_alive`
- `always_show`
- `component`
- `remark`

直接结论：

- Spring Boot 当前没有真实 `menu_key`
- Spring Boot 当前没有 `meta`
- Spring Boot 当前没有 `redirect`
- Spring Boot 当前把 `layout` 语义丢失了

### 5.2 权限模型不等价

NestJS 语义：

- 菜单权限使用 `menuKey`/菜单权限码，例如 `main_system_menu`
- 按钮权限使用业务权限点，例如 `system:user:create`
- `/auth/access` 中菜单码与按钮码是分开的

Spring Boot 当前语义：

- 菜单和接口权限大量使用 `system:user:query`
- 角色/用户上的权限字符串没有分出 NestJS 那套菜单码层

直接结论：

- 如果不补一层“菜单码映射”，Spring Boot 无法等价输出 `menuCodeList`
- 如果不重构权限聚合，`/auth/access` 只能做近似兼容，不能做严格对齐

### 5.3 用户来源不等价

NestJS：

- 登录用户来自数据库
- 菜单可访问性来自数据库角色菜单关联

Spring Boot：

- 当前认证用户主要来自 `application.yml` 中的 `local-users`

直接结论：

- 只改菜单接口而不改认证访问上下文，仍会造成用户级菜单差异

## 6. 建议分阶段实施

### 阶段 1：接口外形先对齐

目标：

- 不立即重构底层权限体系
- 先把前端最直接依赖的菜单接口地址和返回结构收敛

执行项：

- `/api/auth/routes` 返回改成 NestJS 路由树结构
- `/api/auth/access` 返回改成 `user + roles + menuTree`
- `/api/menus/tree` 增加 `view` 和 `roleId`
- `/api/menus` 显式补出 `permissionCode`
- 补齐 `PATCH /api/menus/{id}/status`
- 补齐 `PUT /api/menus/sync`

风险：

- `menuKey` 仍只能通过映射或降级策略生成
- `menuCodeList` 仍可能不是严格 NestJS 语义

### 阶段 2：菜单语义对齐

目标：

- 让 Spring Boot 菜单对象真正具备 NestJS 语义

执行项：

- 为 `sys_menu` 增加 `menu_key`
- 为 `sys_menu` 增加 `redirect`
- 为 `sys_menu` 增加 `layout`
- 为 `sys_menu` 增加 `meta` JSON
- 调整菜单增删改查 DTO 与持久化映射

风险：

- 涉及数据库迁移
- 需要同步种子数据和已有环境数据

### 阶段 3：权限语义对齐

目标：

- 让 `/api/auth/access`、`/api/auth/routes` 的过滤逻辑和 NestJS 真正等价

执行项：

- 引入菜单码与按钮权限分层
- 基于角色菜单关系构造 `menuCodeList`
- 基于角色/直接权限构造 `buttonCodeList`
- 移除 `/auth/routes` 对 `application.yml` 静态路由的依赖
- 逐步淘汰 `local-users` 作为主认证来源

风险：

- 影响认证、授权、角色、菜单四个模块
- 回归面较大

## 7. 第一阶段建议落地顺序

1. 重写 `/api/auth/routes` 响应模型与组装逻辑
2. 重写 `/api/auth/access` 响应模型
3. 改 `/api/menus` 与 `/api/menus/tree` 为 NestJS 风格字段
4. 补 `PATCH /api/menus/{id}/status`
5. 补 `PUT /api/menus/sync`
6. 再处理 `view`、`roleId` 的筛选逻辑

## 8. 当前最重要的判断

如果目标是“可替换 NestJS”，那么不能继续把以下能力视为长期方案：

- `application.yml` 中静态定义 `/auth/routes`
- 用 `permissionCode` 直接充当 `menuKey`
- 将菜单路径/组件硬改成另一套前端写法
- 只返回 `routeCodes/homePath` 的 `/auth/access`

这几项都应视为过渡实现。

## Progress

- [x] Item 1: `/api/auth/routes` response model and assembly logic
- [x] Item 2: `/api/auth/access` response model
- [x] Item 3: `/api/menus` and `/api/menus/tree` NestJS field alignment
- [x] Item 4: `PATCH /api/menus/{id}/status`
- [x] Item 5: `PUT /api/menus/sync`
- [x] Item 6: `view` and `roleId` filtering logic
- [x] Item 7: tighten `POST /api/menus` and `PATCH /api/menus/{id}` permission code semantics
- [x] Item 8: persist `menu_key`, `layout`, `redirect`, and `meta` on `sys_menu`
- [x] Item 9: backfill built-in `sys_menu` contract data for existing seed rows
- [x] Item 10: prefer database-backed auth users and merge active role permissions with local fallback
- [x] Item 11: prefer database-backed auth routes and access snapshots with property fallback
