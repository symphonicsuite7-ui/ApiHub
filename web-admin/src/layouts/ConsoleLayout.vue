<script setup lang="ts">
import { computed, onMounted } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import {
  Bell,
  DataAnalysis,
  Document,
  Grid,
  Monitor,
  Search,
  Setting,
  SwitchButton,
} from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import { getToken } from "@/utils/auth";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const menus = [
  { path: "/dashboard", label: "Dashboard", icon: Monitor },
  { path: "/interfaces", label: "接口管理", icon: Grid },
  { path: "/apps", label: "应用中心", icon: Document },
  { path: "/logs", label: "调用链追踪", icon: Search },
  { path: "/stats", label: "数据统计", icon: DataAnalysis },
  { path: "/settings", label: "系统设置", icon: Setting },
];

const activePath = computed(() => route.path);

function isActive(path: string) {
  if (path === "/dashboard") return activePath.value === path;
  return activePath.value === path || activePath.value.startsWith(path + "/");
}

onMounted(async () => {
  if (getToken() && !userStore.profile) {
    try {
      await userStore.loadProfile();
    } catch {
      userStore.logout();
      router.replace("/login");
    }
  }
});

function go(path: string) {
  router.push(path);
}

function handleCommand(cmd: string) {
  if (cmd === "logout") {
    userStore.logout();
    ElMessage.success("已退出登录");
    router.replace("/login");
  }
  if (cmd === "settings") {
    router.push("/settings");
  }
}
</script>

<template>
  <div class="shell">
    <header class="header">
      <div class="brand" @click="go('/dashboard')">
        <span class="logo">AH</span>
        <span class="name">ApiHub</span>
      </div>
      <el-input class="search" placeholder="搜索接口 / AppId / TraceId" :prefix-icon="Search" />
      <div class="header-right">
        <el-badge :value="3" :max="99" class="bell">
          <el-popover placement="bottom-end" :width="280" trigger="click">
            <template #reference>
              <el-button circle :icon="Bell" />
            </template>
            <div class="notice">
              <p>接口「短信发送」仍处于下线状态</p>
              <p>应用「测试沙箱」配额即将用尽</p>
              <p>近 1 小时出现 3 次 429 限流</p>
            </div>
          </el-popover>
        </el-badge>
        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user">
            <el-avatar :size="32">{{ userStore.displayName.slice(0, 1).toUpperCase() }}</el-avatar>
            <div class="user-meta">
              <strong>{{ userStore.displayName }}</strong>
              <span>{{ userStore.rolesText }}</span>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings">系统设置</el-dropdown-item>
              <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <aside class="sidebar">
      <button
        v-for="item in menus"
        :key="item.path"
        class="nav-item"
        :class="{ active: isActive(item.path) }"
        type="button"
        @click="go(item.path)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </button>
    </aside>

    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: var(--sidebar-w) 1fr;
  grid-template-rows: var(--header-h) 1fr;
}

.header {
  grid-column: 1 / -1;
  height: var(--header-h);
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 0 20px;
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 180px;
  cursor: pointer;
}

.logo {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: var(--primary);
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
}

.name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.4px;
}

.search {
  width: 360px;
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 16px;
}

.user {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-meta strong {
  font-size: 13px;
}

.user-meta span {
  font-size: 11px;
  color: var(--text-muted);
}

.sidebar {
  background: var(--bg-elevated);
  border-right: 1px solid var(--border);
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  position: relative;
}

.nav-item:hover {
  background: var(--bg-subtle);
  color: var(--text-primary);
}

.nav-item.active {
  background: rgba(37, 99, 235, 0.16);
  color: #fff;
}

.nav-item.active::before {
  content: "";
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 99px;
  background: var(--primary);
}

.main {
  min-width: 0;
  overflow: auto;
  background: var(--bg-base);
}

.notice p {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.notice p:last-child {
  margin-bottom: 0;
}
</style>
