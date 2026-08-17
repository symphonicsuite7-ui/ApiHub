import type { BreadcrumbItem } from "./index";

declare module "vue-router" {
  interface RouteMeta {
    /** 页面标题 */
    title?: string;
    /** 无需登录 */
    public?: boolean;
    /** 允许访问的角色 */
    roles?: string[];
    /** 所需权限标识 */
    permissions?: string[];
    /** 自定义面包屑 */
    breadcrumb?: BreadcrumbItem[];
    /** 是否在面包屑中隐藏 */
    hideInBreadcrumb?: boolean;
  }
}

export {};
