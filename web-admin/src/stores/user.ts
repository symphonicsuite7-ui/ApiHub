import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { fetchMe, login as loginApi } from "@/api/auth";
import { Perm, permissionsOfRoles, roleLabel } from "@/config/permissions";
import { clearToken, setToken } from "@/utils/auth";
import { hasPermission, hasRole } from "@/utils/permission";
import type { LoginVO } from "@/types";

export const useUserStore = defineStore("user", () => {
  const profile = ref<LoginVO | null>(null);

  const displayName = computed(() => profile.value?.nickname || profile.value?.username || "未登录");
  const roles = computed(() => {
    const list = profile.value?.roles || [];
    return list.length ? list : ["USER"];
  });
  const rolesText = computed(() =>
    roles.value.length ? roles.value.map(roleLabel).join(" / ") : roleLabel("USER")
  );
  const isAdmin = computed(() => roles.value.includes("ADMIN"));

  /** 当前用户拥有的权限标识（优先登录接口，否则按角色表回退） */
  const permissions = computed(() => {
    if (profile.value?.permissions?.length) {
      return profile.value.permissions;
    }
    return permissionsOfRoles(roles.value);
  });

  const canAccessSettings = computed(() =>
    hasRole(roles.value, ["ADMIN"]) && hasPermission(permissions.value, [Perm.SYSTEM_SETTINGS])
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
