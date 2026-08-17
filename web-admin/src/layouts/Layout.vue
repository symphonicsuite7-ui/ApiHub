<script setup lang="ts">
import { RouterView, useRouter } from "vue-router";
import { Bell, SwitchButton } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { provideLayout } from "@/composables/useLayout";
import { useUserStore } from "@/stores/user";
import AppHeader from "./components/Header.vue";
import AppSidebar from "./components/Sidebar.vue";
import AppBreadcrumb from "./components/Breadcrumb.vue";

provideLayout();

const router = useRouter();
const userStore = useUserStore();

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
  <div class="app-layout">
    <AppHeader>
      <template #actions>
        <el-badge :value="3" :max="99">
          <el-popover placement="bottom-end" :width="280" trigger="click">
            <template #reference>
              <el-button circle :icon="Bell" />
            </template>
            <div class="notice-list">
              <p>接口「短信发送」仍处于下线状态</p>
              <p>应用「测试沙箱」配额即将用尽</p>
              <p>近 1 小时出现 3 次 429 限流</p>
            </div>
          </el-popover>
        </el-badge>

        <el-dropdown trigger="click" @command="handleCommand">
          <div class="user-trigger">
            <el-avatar :size="32">{{ userStore.displayName.slice(0, 1).toUpperCase() }}</el-avatar>
            <div class="user-meta">
              <strong>{{ userStore.displayName }}</strong>
              <span>{{ userStore.rolesText }}</span>
            </div>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="userStore.canAccessSettings" command="settings">
                系统设置
              </el-dropdown-item>
              <el-dropdown-item command="logout" :icon="SwitchButton">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
    </AppHeader>

    <div class="app-body">
      <AppSidebar />

      <div class="app-content">
        <AppBreadcrumb />
        <main class="app-main">
          <RouterView />
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
@use "@/styles/variables" as *;

.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-base);
}

.app-body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.app-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.app-main {
  flex: 1;
  overflow: auto;
  background: var(--bg-base);
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.user-meta {
  display: flex;
  flex-direction: column;
  line-height: $line-height-base;

  @include respond-down(md) {
    display: none;
  }
}

.user-meta strong {
  font-size: var(--font-size-sm);
}

.user-meta span {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.notice-list p {
  margin: 0 0 8px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.notice-list p:last-child {
  margin-bottom: 0;
}
</style>
