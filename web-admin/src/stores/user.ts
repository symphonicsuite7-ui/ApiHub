import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { fetchMe, login as loginApi } from "@/api/auth";
import { clearToken, setToken } from "@/utils/auth";
import type { LoginVO } from "@/types";

export const useUserStore = defineStore("user", () => {
  const profile = ref<LoginVO | null>(null);

  const displayName = computed(() => profile.value?.nickname || profile.value?.username || "未登录");
  const rolesText = computed(() => (profile.value?.roles || []).join(" / ") || "USER");
  const isAdmin = computed(() => (profile.value?.roles || []).includes("ADMIN"));

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

  return { profile, displayName, rolesText, isAdmin, login, loadProfile, logout };
});
