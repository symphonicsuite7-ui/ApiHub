import type { Component } from "vue";
import {
  DataAnalysis,
  Document,
  Grid,
  Monitor,
  Search,
  Setting,
} from "@element-plus/icons-vue";

/** 导航项（菜单 + 路由 meta 单一数据源） */
export interface NavItem {
  path: string;
  menuLabel: string;
  pageTitle: string;
  icon: Component;
  roles?: string[];
  permissions?: string[];
  hidden?: boolean;
}

export const NAV_ITEMS: NavItem[] = [
  {
    path: "/dashboard",
    menuLabel: "控制台",
    pageTitle: "控制台",
    icon: Monitor,
  },
  {
    path: "/interfaces",
    menuLabel: "接口管理",
    pageTitle: "接口资产中心",
    icon: Grid,
  },
  {
    path: "/apps",
    menuLabel: "应用中心",
    pageTitle: "应用中心",
    icon: Document,
  },
  {
    path: "/logs",
    menuLabel: "调用链追踪",
    pageTitle: "调用链追踪",
    icon: Search,
  },
  {
    path: "/stats",
    menuLabel: "数据分析",
    pageTitle: "数据分析",
    icon: DataAnalysis,
  },
  {
    path: "/settings",
    menuLabel: "系统设置",
    pageTitle: "系统设置",
    icon: Setting,
    roles: ["ADMIN"],
    permissions: ["system:settings"],
  },
];

/** 根据路径查找导航配置 */
export function findNavByPath(path: string): NavItem | undefined {
  return NAV_ITEMS.find((item) => item.path === path || path.startsWith(`${item.path}/`));
}
