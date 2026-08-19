import type { Component } from "vue";
import {
  DataAnalysis,
  Document,
  Grid,
  Monitor,
  Search,
  Setting,
} from "@element-plus/icons-vue";

import { Perm, Role } from "@/config/permissions";

/** 导航项（菜单 + 路由 meta 单一数据源） */
export interface NavItem {
  path: string;
  menuLabel: string;
  /** 普通用户侧栏文案，缺省则用 menuLabel */
  userMenuLabel?: string;
  pageTitle: string;
  /** 普通用户页面标题，缺省则用 pageTitle */
  userPageTitle?: string;
  icon: Component;
  roles?: string[];
  permissions?: string[];
  hidden?: boolean;
}

export const NAV_ITEMS: NavItem[] = [
  {
    path: "/dashboard",
    menuLabel: "控制台",
    userMenuLabel: "工作台",
    pageTitle: "管理控制台",
    userPageTitle: "开发者工作台",
    icon: Monitor,
  },
  {
    path: "/interfaces",
    menuLabel: "接口管理",
    userMenuLabel: "接口市场",
    pageTitle: "接口资产中心",
    userPageTitle: "开放接口市场",
    icon: Grid,
    permissions: [Perm.API_VIEW],
  },
  {
    path: "/apps",
    menuLabel: "应用中心",
    userMenuLabel: "我的应用",
    pageTitle: "应用中心",
    userPageTitle: "我的应用",
    icon: Document,
    permissions: [Perm.APP_VIEW],
  },
  {
    path: "/logs",
    menuLabel: "调用链追踪",
    userMenuLabel: "我的调用",
    pageTitle: "调用链追踪",
    userPageTitle: "我的调用记录",
    icon: Search,
    permissions: [Perm.LOG_VIEW],
  },
  {
    path: "/stats",
    menuLabel: "数据分析",
    userMenuLabel: "我的用量",
    pageTitle: "平台数据分析",
    userPageTitle: "我的用量统计",
    icon: DataAnalysis,
    permissions: [Perm.STATS_VIEW],
  },
  {
    path: "/settings",
    menuLabel: "系统设置",
    pageTitle: "系统设置",
    icon: Setting,
    roles: [Role.ADMIN],
    permissions: [Perm.SYSTEM_SETTINGS],
  },
];

/** 按角色解析菜单名 */
export function resolveNavLabel(item: NavItem, isAdmin: boolean): string {
  return isAdmin ? item.menuLabel : item.userMenuLabel || item.menuLabel;
}

/** 按角色解析页面标题 */
export function resolveNavTitle(item: NavItem, isAdmin: boolean): string {
  return isAdmin ? item.pageTitle : item.userPageTitle || item.pageTitle;
}

/** 根据路径查找导航配置 */
export function findNavByPath(path: string): NavItem | undefined {
  return NAV_ITEMS.find((item) => item.path === path || path.startsWith(`${item.path}/`));
}
