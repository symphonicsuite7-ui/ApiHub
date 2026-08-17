<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { filterMenus, isMenuActive } from "@/utils/permission";
import { menuConfig } from "@/config/menu";
import { useLayout } from "@/composables/useLayout";
import { useUserStore } from "@/stores/user";
import type { MenuItem } from "@/config/menu";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const { sidebarCollapsed, sidebarMobileOpen, closeMobileSidebar } = useLayout();

const visibleMenus = computed(() =>
  filterMenus(menuConfig, userStore.roles, userStore.permissions)
);

const currentPath = computed(() => route.path);

function navigate(item: MenuItem) {
  router.push(item.path);
  closeMobileSidebar();
}

function checkActive(path: string) {
  return isMenuActive(path, currentPath.value);
}
</script>

<template>
  <div
    class="sidebar-mask"
    :class="{ show: sidebarMobileOpen }"
    @click="closeMobileSidebar"
  />

  <aside
    class="app-sidebar"
    :class="{
      collapsed: sidebarCollapsed && !sidebarMobileOpen,
      'mobile-open': sidebarMobileOpen,
    }"
  >
    <nav class="sidebar-nav">
      <button
        v-for="item in visibleMenus"
        :key="item.path"
        class="nav-item"
        :class="{ active: checkActive(item.path) }"
        type="button"
        :title="sidebarCollapsed ? item.label : undefined"
        @click="navigate(item)"
      >
        <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
        <span class="nav-label">{{ item.label }}</span>
      </button>
    </nav>

    <div class="sidebar-footer">
      <span class="version">ApiHub v1.0</span>
    </div>
  </aside>
</template>

<style scoped lang="scss">
@use "@/styles/variables" as *;

.sidebar-mask {
  display: none;
  position: fixed;
  inset: 0;
  background: var(--bg-overlay);
  z-index: 190;
  opacity: 0;
  pointer-events: none;
  transition: opacity var(--transition-base);

  &.show {
    @include respond-down(md) {
      display: block;
      opacity: 1;
      pointer-events: auto;
    }
  }
}

.app-sidebar {
  width: var(--sidebar-w);
  background: var(--bg-elevated);
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-base), transform var(--transition-base);
  flex-shrink: 0;
  z-index: 200;

  &.collapsed {
    width: var(--sidebar-collapsed-w);

    .nav-label,
    .sidebar-footer {
      opacity: 0;
      width: 0;
      overflow: hidden;
    }

    .nav-item {
      justify-content: center;
      padding-inline: 0;
    }
  }

  @include respond-down(md) {
    position: fixed;
    top: var(--header-h);
    left: 0;
    bottom: 0;
    transform: translateX(-100%);
    box-shadow: $shadow-sidebar;

    &.mobile-open {
      transform: translateX(0);
    }
  }
}

.sidebar-nav {
  flex: 1;
  padding: 16px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
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
  border-radius: var(--radius-md);
  cursor: pointer;
  font-size: var(--font-size-base);
  position: relative;
  transition: background var(--transition-fast), color var(--transition-fast);
  white-space: nowrap;

  &:hover {
    background: $color-sidebar-hover-bg;
    color: var(--text-primary);
  }

  &.active {
    background: $color-sidebar-active-bg;
    color: #fff;

    &::before {
      content: "";
      position: absolute;
      left: 0;
      top: 8px;
      bottom: 8px;
      width: 3px;
      border-radius: var(--radius-full);
      background: var(--primary);
    }
  }
}

.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.nav-label {
  transition: opacity var(--transition-fast), width var(--transition-fast);
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border);
  transition: opacity var(--transition-fast);
}

.version {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}
</style>
