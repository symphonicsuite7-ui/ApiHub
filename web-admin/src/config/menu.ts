import type { Component } from "vue";
import { NAV_ITEMS, type NavItem } from "@/config/navigation";

/** 菜单项配置（由 navigation 派生，避免双份维护） */
export interface MenuItem {
  path: string;
  label: string;
  userLabel?: string;
  icon: Component;
  roles?: string[];
  permissions?: string[];
  hidden?: boolean;
  children?: MenuItem[];
}

function toMenuItem(item: NavItem): MenuItem {
  return {
    path: item.path,
    label: item.menuLabel,
    userLabel: item.userMenuLabel,
    icon: item.icon,
    roles: item.roles,
    permissions: item.permissions,
    hidden: item.hidden,
  };
}

/** 侧栏菜单配置 */
export const menuConfig: MenuItem[] = NAV_ITEMS.map(toMenuItem);
