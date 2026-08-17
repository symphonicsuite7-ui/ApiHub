import { inject, provide, ref, type InjectionKey, type Ref } from "vue";

export interface LayoutContext {
  /** 桌面端侧栏折叠 */
  sidebarCollapsed: Ref<boolean>;
  /** 移动端侧栏抽屉 */
  sidebarMobileOpen: Ref<boolean>;
  toggleSidebar: () => void;
  closeMobileSidebar: () => void;
}

const LayoutKey: InjectionKey<LayoutContext> = Symbol("layout");

/** 在 Layout 根组件中提供布局状态 */
export function provideLayout(): LayoutContext {
  const sidebarCollapsed = ref(false);
  const sidebarMobileOpen = ref(false);

  const ctx: LayoutContext = {
    sidebarCollapsed,
    sidebarMobileOpen,
    toggleSidebar() {
      if (window.innerWidth <= 960) {
        sidebarMobileOpen.value = !sidebarMobileOpen.value;
      } else {
        sidebarCollapsed.value = !sidebarCollapsed.value;
      }
    },
    closeMobileSidebar() {
      sidebarMobileOpen.value = false;
    },
  };

  provide(LayoutKey, ctx);
  return ctx;
}

/** 在 Header / Sidebar 中消费布局状态 */
export function useLayout(): LayoutContext {
  const ctx = inject(LayoutKey);
  if (!ctx) {
    throw new Error("useLayout 必须在 Layout 组件内使用");
  }
  return ctx;
}
