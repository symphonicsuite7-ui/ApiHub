import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { fetchMe, login as loginApi } from "@/api/auth";
import { clearToken, setToken } from "@/utils/auth";
import { hasPermission, hasRole } from "@/utils/permission";
import type { LoginVO } from "@/types";

/** 角色默认权限映射（演示，后续对接后端 RBAC） */
const rolePermissionMap: Record<string, string[]> = {
  ADMIN: ["system:settings", "system:user", "api:manage", "app:manage"],
  USER: ["api:view", "app:view", "log:view"],
};

export const useUserStore = defineStore("user", () => {
  const profile = ref<LoginVO | null>(null);

  const displayName = computed(() => profile.value?.nickname || profile.value?.username || "未登录");
  const roles = computed(() => profile.value?.roles || []);
  const rolesText = computed(() => roles.value.join(" / ") || "USER");
  const isAdmin = computed(() => roles.value.includes("ADMIN"));

  /** 当前用户拥有的权限标识（优先 API，回退角色映射） */
  const permissions = computed(() => {
    if (profile.value?.permissions?.length) {
      return profile.value.permissions;
    }
    const set = new Set<string>();
    roles.value.forEach((role) => {
      (rolePermissionMap[role] || []).forEach((p) => set.add(p));
    });
    return Array.from(set);
  });

  const canAccessSettings = computed(() =>
    hasRole(roles.value, ["ADMIN"]) && hasPermission(permissions.value, ["system:settings"])
  );

  async function login(username: string, password: string) {
    const res = await loginApi({ username, password });
    const data = res.data.data;
    setToken(data.token);
    profile.value = data;
    return data;
  }

  async function loadProfile() {
    const res = await fetchMe();
    profile.value = res.data.data;
    if (profile.value.token) {
      setToken(profile.value.token);
    }
    return profile.value;
  }

  function logout() {
    clearToken();
    profile.value = null;
  }

  return {
    profile,
    displayName,
    roles,
    rolesText,
    permissions,
    isAdmin,
    canAccessSettings,
    login,
    loadProfile,
    logout,
  };
});
