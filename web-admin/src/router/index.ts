import { createRouter, createWebHistory } from "vue-router";
import { findNavByPath, NAV_ITEMS, resolveNavTitle } from "@/config/navigation";
import { getToken } from "@/utils/auth";
import { hasPermission, hasRole } from "@/utils/permission";
import { useUserStore } from "@/stores/user";

function navMeta(index: number, extra: Record<string, unknown> = {}) {
  const item = NAV_ITEMS[index];
  return {
    title: item.pageTitle,
    roles: item.roles,
    permissions: item.permissions,
    ...extra,
  };
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("@/views/login/index.vue"),
      meta: { public: true, title: "登录" },
    },
    {
      path: "/",
      component: () => import("@/layouts/Layout.vue"),
      redirect: "/dashboard",
      children: [
        {
          path: "dashboard",
          name: "dashboard",
          component: () => import("@/views/dashboard/index.vue"),
          meta: navMeta(0),
        },
        {
          path: "interfaces",
          name: "interfaces",
          component: () => import("@/views/interfaces/index.vue"),
          meta: navMeta(1),
        },
        {
          path: "interfaces/:id",
          name: "interface-detail",
          component: () => import("@/views/interfaces/detail.vue"),
          meta: {
            ...navMeta(1),
            title: "接口详情",
            breadcrumb: [
              { title: NAV_ITEMS[1].pageTitle, path: "/interfaces" },
              { title: "接口详情" },
            ],
          },
        },
        {
          path: "apps",
          name: "apps",
          component: () => import("@/views/apps/index.vue"),
          meta: navMeta(2),
        },
        {
          path: "logs",
          name: "logs",
          component: () => import("@/views/logs/index.vue"),
          meta: navMeta(3),
        },
        {
          path: "stats",
          name: "stats",
          component: () => import("@/views/stats/index.vue"),
          meta: navMeta(4),
        },
        {
          path: "settings",
          name: "settings",
          component: () => import("@/views/settings/index.vue"),
          meta: navMeta(5),
        },
        {
          path: "403",
          name: "forbidden",
          component: () => import("@/views/forbidden/index.vue"),
          meta: { title: "无权访问", hideInBreadcrumb: true },
        },
      ],
    },
    { path: "/:pathMatch(.*)*", redirect: "/dashboard" },
  ],
});

router.beforeEach(async (to) => {
  const token = getToken();

  if (to.meta.public) {
    if (token && to.path === "/login") {
      return "/dashboard";
    }
    document.title = `${to.meta.title || "登录"} · ApiHub`;
    return true;
  }

  if (!token) {
    return "/login";
  }

  const userStore = useUserStore();

  if (!userStore.profile) {
    try {
      await userStore.loadProfile();
    } catch {
      userStore.logout();
      return "/login";
    }
  }

  if (to.name === "forbidden") {
    document.title = "无权访问 · ApiHub";
    return true;
  }

  const nav = findNavByPath(to.path);
  const requiredRoles = to.meta.roles || nav?.roles;
  const requiredPerms = to.meta.permissions || nav?.permissions;

  if (requiredRoles && !hasRole(userStore.roles, requiredRoles)) {
    return { path: "/403", query: { from: to.fullPath } };
  }

  if (requiredPerms && !hasPermission(userStore.permissions, requiredPerms)) {
    return { path: "/403", query: { from: to.fullPath } };
  }

  const pageTitle = nav
    ? resolveNavTitle(nav, userStore.isAdmin)
    : (to.meta.title as string) || "控制台";
  if (to.name === "interface-detail") {
    const listTitle = resolveNavTitle(NAV_ITEMS[1], userStore.isAdmin);
    to.meta.breadcrumb = [
      { title: listTitle, path: "/interfaces" },
      { title: "接口详情" },
    ];
    to.meta.title = "接口详情";
    document.title = `接口详情 · ApiHub`;
  } else {
    to.meta.title = pageTitle;
    document.title = `${pageTitle} · ApiHub`;
  }

  return true;
});

export default router;

