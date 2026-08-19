import { computed } from "vue";
import {
  ADMIN_WORKSPACE,
  Perm,
  Role,
  USER_WORKSPACE,
  type WorkspaceCopy,
} from "@/config/permissions";
import { hasPermission, hasRole } from "@/utils/permission";
import { useUserStore } from "@/stores/user";

/**
 * 前端权限入口：按钮用 can()，页面用路由守卫。
 * 当前不对接新的后端权限接口，角色仍来自登录返回值。
 */
export function useAccess() {
  const userStore = useUserStore();

  const isAdmin = computed(() => userStore.isAdmin);
  const workspace = computed<WorkspaceCopy>(() =>
    isAdmin.value ? ADMIN_WORKSPACE : USER_WORKSPACE
  );

  function can(permission: string): boolean {
    return hasPermission(userStore.permissions, [permission]);
  }

  function canAny(permissions: string[]): boolean {
    return hasPermission(userStore.permissions, permissions);
  }

  function canRole(role: string): boolean {
    return hasRole(userStore.roles, [role]);
  }

  return {
    Perm,
    Role,
    isAdmin,
    workspace,
    can,
    canAny,
    canRole,
  };
}
