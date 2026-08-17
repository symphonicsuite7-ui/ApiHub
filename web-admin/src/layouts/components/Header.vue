<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { Expand, Fold, Menu } from "@element-plus/icons-vue";
import { useLayout } from "@/composables/useLayout";

const router = useRouter();
const { sidebarCollapsed, sidebarMobileOpen, toggleSidebar } = useLayout();

const isMobile = ref(false);

const isMobileOpen = computed(() => sidebarMobileOpen.value);

const toggleIcon = computed(() => {
  if (isMobile.value) {
    return isMobileOpen.value ? Fold : Menu;
  }
  return sidebarCollapsed.value ? Expand : Fold;
});

function updateViewport() {
  isMobile.value = window.innerWidth <= 960;
}

onMounted(() => {
  updateViewport();
  window.addEventListener("resize", updateViewport);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", updateViewport);
});

function goHome() {
  router.push("/dashboard");
}
</script>

<template>
  <header class="app-header">
    <div class="header-left">
      <button class="menu-toggle" type="button" aria-label="切换菜单" @click="toggleSidebar">
        <el-icon :size="18">
          <component :is="toggleIcon" />
        </el-icon>
      </button>

      <div class="brand" @click="goHome">
        <span class="logo">AH</span>
        <span class="brand-name">ApiHub</span>
      </div>
    </div>

    <div class="header-search">
      <el-input placeholder="搜索接口 / AppId / TraceId" clearable />
    </div>

    <div class="header-right">
      <slot name="actions" />
    </div>
  </header>
</template>

<style scoped lang="scss">
@use "@/styles/variables" as *;

.app-header {
  height: var(--header-h);
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 20px;
  background: var(--bg-elevated);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.menu-toggle {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  transition: background var(--transition-fast), color var(--transition-fast);

  &:hover {
    background: var(--bg-subtle);
    color: var(--text-primary);
  }
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.logo {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-md);
  background: var(--primary);
  display: grid;
  place-items: center;
  font-size: var(--font-size-xs);
  font-weight: $font-weight-bold;
  color: #fff;
}

.brand-name {
  font-size: var(--font-size-lg);
  font-weight: $font-weight-bold;
  letter-spacing: 0.4px;

  @include respond-down(md) {
    display: none;
  }
}

.header-search {
  flex: 1;
  max-width: 420px;

  @include respond-down(md) {
    display: none;
  }
}

.header-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
</style>
