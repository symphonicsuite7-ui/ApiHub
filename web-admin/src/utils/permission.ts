import type { MenuItem } from "@/config/menu";

/** 判断用户是否拥有指定角色之一 */
export function hasRole(userRoles: string[], required?: string[]): boolean {
  if (!required || required.length === 0) return true;
  return required.some((role) => userRoles.includes(role));
}

/** 判断用户是否拥有指定权限之一 */
export function hasPermission(userPermissions: string[], required?: string[]): boolean {
  if (!required || required.length === 0) return true;
  return required.some((perm) => userPermissions.includes(perm));
}

/** 菜单项是否对当前用户可见 */
export function isMenuVisible(
  item: MenuItem,
  userRoles: string[],
  userPermissions: string[] = []
): boolean {
  if (item.hidden) return false;
  if (!hasRole(userRoles, item.roles)) return false;
  if (!hasPermission(userPermissions, item.permissions)) return false;
  return true;
}

/** 按角色/权限过滤菜单树 */
export function filterMenus(
  menus: MenuItem[],
  userRoles: string[],
  userPermissions: string[] = []
): MenuItem[] {
  return menus
    .filter((item) => isMenuVisible(item, userRoles, userPermissions))
    .map((item) => ({
      ...item,
      children: item.children
        ? filterMenus(item.children, userRoles, userPermissions)
        : undefined,
    }));
}

/** 路由是否匹配菜单项（含子路径高亮） */
export function isMenuActive(menuPath: string, currentPath: string): boolean {
  if (menuPath === "/dashboard") return currentPath === menuPath;
  return currentPath === menuPath || currentPath.startsWith(`${menuPath}/`);
}
