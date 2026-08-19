/** 权限标识：页面与按钮统一用这些常量，避免散落字符串 */
export const Perm = {
  API_VIEW: "api:view",
  API_MANAGE: "api:manage",
  APP_VIEW: "app:view",
  APP_CREATE: "app:create",
  APP_MANAGE: "app:manage",
  LOG_VIEW: "log:view",
  LOG_VIEW_ALL: "log:view-all",
  STATS_VIEW: "stats:view",
  STATS_PLATFORM: "stats:platform",
  OPS_HEALTH: "ops:health",
  SYSTEM_SETTINGS: "system:settings",
} as const;

export type PermissionCode = (typeof Perm)[keyof typeof Perm];

export const Role = {
  ADMIN: "ADMIN",
  USER: "USER",
} as const;

export type RoleCode = (typeof Role)[keyof typeof Role];

/** 角色中文名，用于顶栏与页面文案 */
export const ROLE_LABELS: Record<string, string> = {
  ADMIN: "管理员",
  USER: "开发者",
};

/**
 * 前端演示用角色权限表。后端 RBAC 接入后，优先用登录接口返回的 permissions。
 */
export const ROLE_PERMISSIONS: Record<string, string[]> = {
  ADMIN: [
    Perm.API_VIEW,
    Perm.API_MANAGE,
    Perm.APP_VIEW,
    Perm.APP_CREATE,
    Perm.APP_MANAGE,
    Perm.LOG_VIEW,
    Perm.LOG_VIEW_ALL,
    Perm.STATS_VIEW,
    Perm.STATS_PLATFORM,
    Perm.OPS_HEALTH,
    Perm.SYSTEM_SETTINGS,
  ],
  USER: [
    Perm.API_VIEW,
    Perm.APP_VIEW,
    Perm.APP_CREATE,
    Perm.LOG_VIEW,
    Perm.STATS_VIEW,
  ],
};

/** 工作台视觉文案：管理员看平台运营，普通用户看开发者门户 */
export interface WorkspaceCopy {
  name: string;
  badge: string;
  dashboardTitle: string;
  dashboardDesc: string;
}

export const ADMIN_WORKSPACE: WorkspaceCopy = {
  name: "管理控制台",
  badge: "ADMIN",
  dashboardTitle: "管理控制台",
  dashboardDesc: "平台运营总览 · 接口资产 · 全链路审计",
};

export const USER_WORKSPACE: WorkspaceCopy = {
  name: "开发者工作台",
  badge: "DEVELOPER",
  dashboardTitle: "开发者工作台",
  dashboardDesc: "浏览开放接口、管理我的应用与调用用量",
};

export function roleLabel(role: string): string {
  return ROLE_LABELS[role] || role;
}

export function permissionsOfRoles(roles: string[]): string[] {
  const set = new Set<string>();
  roles.forEach((role) => {
    (ROLE_PERMISSIONS[role] || []).forEach((perm) => set.add(perm));
  });
  return Array.from(set);
}
