package com.luckycolor.admin.modules.system.menu.support;

import com.luckycolor.admin.modules.system.menu.dataobject.MenuDO;
import java.util.Map;
import org.springframework.util.StringUtils;

public final class FrontendMenuContractMapper {

    private FrontendMenuContractMapper() {
    }

    public static String resolveRouteName(MenuDO menu) {
        String menuKey = resolveMenuKey(menu);
        return switch (menuKey) {
            case "main_analysis" -> "dashboardIndex";
            case "main_system" -> "systemManagement";
            case "main_system_users" -> "systemUsers";
            case "main_system_role" -> "systemRole";
            case "main_system_menu" -> "systemMenu";
            case "main_system_department" -> "systemDepartment";
            case "icomponent_dict", "main_system_dict" -> "systemDict";
            case "main_system_config" -> "systemConfig";
            case "main_system_notice" -> "systemNotice";
            case "main_system_tenant_root" -> "tenantCenter";
            case "main_system_tenant" -> "tenantManagement";
            case "main_system_tenant_package" -> "tenantPackage";
            case "main_system_codegen" -> "toolCodegen";
            default -> StringUtils.hasText(menu.getRouteName()) ? menu.getRouteName().trim() : "menu" + menu.getId();
        };
    }

    public static String resolvePath(MenuDO menu, Map<Long, MenuDO> menusById) {
        String menuKey = resolveMenuKey(menu);
        return switch (menuKey) {
            case "main_analysis" -> "/index";
            case "main_system" -> "/systemManagement";
            case "main_system_users" -> "/systemManagement/system/users";
            case "main_system_role" -> "/systemManagement/system/role";
            case "main_system_menu" -> "/systemManagement/system/menu";
            case "main_system_department" -> "/systemManagement/system/department";
            case "icomponent_dict", "main_system_dict" -> "/systemManagement/system/dict";
            case "main_system_config" -> "/systemManagement/system/config";
            case "main_system_notice" -> "/systemManagement/system/notice";
            case "main_system_tenant_root" -> "/tenantCenter";
            case "main_system_tenant" -> "/tenantCenter/tenant";
            case "main_system_tenant_package" -> "/tenantCenter/tenantPackage";
            case "main_system_codegen" -> "/tool/codegen";
            default -> buildFallbackPath(menu, menusById);
        };
    }

    public static String resolveComponent(MenuDO menu) {
        String menuKey = resolveMenuKey(menu);
        return switch (menuKey) {
            case "main_analysis" -> "index/index";
            case "main_system", "main_system_tenant_root" -> "sys/index";
            case "main_system_users" -> "sys/user";
            case "main_system_role" -> "sys/role/index";
            case "main_system_menu" -> "sys/menu/index";
            case "main_system_department" -> "sys/department/department";
            case "icomponent_dict", "main_system_dict" -> "sys/dict/index";
            case "main_system_config" -> "sys/config/index";
            case "main_system_notice" -> "sys/notice/index";
            case "main_system_tenant" -> "sys/tenant/index";
            case "main_system_tenant_package" -> "sys/tenantPackage/index";
            case "main_system_codegen" -> "tool/codegen/index";
            default -> resolveFallbackComponent(menu);
        };
    }

    public static String resolveRedirect(MenuDO menu) {
        String menuKey = resolveMenuKey(menu);
        return switch (menuKey) {
            case "main_system" -> "/systemManagement/system/users";
            case "main_system_tenant_root" -> "/tenantCenter/tenant";
            default -> normalizeRedirect(menu == null ? null : menu.getRedirect());
        };
    }

    public static String resolveMenuKey(MenuDO menu) {
        if (menu == null) {
            return null;
        }
        if (StringUtils.hasText(menu.getMenuKey())) {
            return menu.getMenuKey().trim();
        }
        if (StringUtils.hasText(menu.getPermissionCode())) {
            return menu.getPermissionCode().trim();
        }
        if (StringUtils.hasText(menu.getRouteName())) {
            return menu.getRouteName().trim();
        }
        return "menu:" + menu.getId();
    }

    private static String resolveFallbackComponent(MenuDO menu) {
        String component = menu == null ? null : menu.getComponent();
        if (!StringUtils.hasText(component)) {
            return "sys/index";
        }
        String normalized = component.trim();
        return switch (normalized) {
            case "Layout", "sys", "system/index", "system", "tenant/index", "tenant", "tool/index", "tool" -> "sys/index";
            case "dashboard/index" -> "index/index";
            case "system/user/index", "system/users/index" -> "sys/user";
            case "system/role/index", "system/roles/index" -> "sys/role/index";
            case "system/menu/index", "system/menus/index" -> "sys/menu/index";
            case "system/department/index", "system/departments/index" -> "sys/department/department";
            case "system/dictionary/index", "system/dict/index" -> "sys/dict/index";
            case "system/config/index", "system/configs/index" -> "sys/config/index";
            case "system/notice/index", "system/notices/index" -> "sys/notice/index";
            case "tenant/tenant/index", "system/tenant/index", "sys/tenant/index" -> "sys/tenant/index";
            case "tenant/package/index", "system/tenantPackage/index", "sys/tenantPackage/index" -> "sys/tenantPackage/index";
            case "platform/codegen/index", "tool/codegen/index" -> "tool/codegen/index";
            default -> normalized;
        };
    }

    private static String buildFallbackPath(MenuDO menu, Map<Long, MenuDO> menusById) {
        if (menu == null) {
            return "/";
        }
        String currentPath = menu.getRoutePath();
        if (!StringUtils.hasText(currentPath)) {
            return "/";
        }
        String normalizedPath = normalizePath(currentPath);
        if (currentPath.trim().startsWith("/")) {
            return normalizedPath;
        }
        Long parentId = normalizeParentId(menu.getParentId());
        if (parentId == 0L) {
            return normalizedPath;
        }
        MenuDO parent = menusById == null ? null : menusById.get(parentId);
        if (parent == null) {
            return normalizedPath;
        }
        String parentPath = resolvePath(parent, menusById);
        return normalizePath(parentPath + "/" + currentPath.trim());
    }

    private static String normalizeRedirect(String redirect) {
        if (!StringUtils.hasText(redirect)) {
            return null;
        }
        return switch (redirect.trim()) {
            case "/dashboard" -> "/index";
            case "/system/users" -> "/systemManagement/system/users";
            case "/system/roles" -> "/systemManagement/system/role";
            case "/system/menu" -> "/systemManagement/system/menu";
            case "/system/departments" -> "/systemManagement/system/department";
            case "/system/dict" -> "/systemManagement/system/dict";
            case "/system/config" -> "/systemManagement/system/config";
            case "/system/notices" -> "/systemManagement/system/notice";
            case "/tenant/tenants" -> "/tenantCenter/tenant";
            case "/tenant/packages" -> "/tenantCenter/tenantPackage";
            default -> normalizePath(redirect);
        };
    }

    private static String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "/";
        }
        String normalized = path.trim().replace('\\', '/');
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.replaceAll("/+", "/");
    }

    private static Long normalizeParentId(Long parentId) {
        return parentId == null ? 0L : parentId;
    }
}
