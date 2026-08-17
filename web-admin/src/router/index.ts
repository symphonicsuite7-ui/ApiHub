import { createRouter, createWebHistory } from "vue-router";
import { NAV_ITEMS } from "@/config/navigation";
import { getToken } from "@/utils/auth";
import { hasPermission, hasRole } from "@/utils/permission";
import { useUserStore } from "@/stores/user";

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
          meta: { title: NAV_ITEMS[0].pageTitle },
        },
        {
          path: "interfaces",
          name: "interfaces",
          component: () => import("@/views/interfaces/index.vue"),
          meta: { title: NAV_ITEMS[1].pageTitle },
        },
        {
          path: "interfaces/:id",
          name: "interface-detail",
          component: () => import("@/views/interfaces/detail.vue"),
          meta: {
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
          meta: { title: NAV_ITEMS[2].pageTitle },
        },
        {
          path: "logs",
          name: "logs",
          component: () => import("@/views/logs/index.vue"),
          meta: { title: NAV_ITEMS[3].pageTitle },
        },
        {
          path: "stats",
          name: "stats",
          component: () => import("@/views/stats/index.vue"),
          meta: { title: NAV_ITEMS[4].pageTitle },
        },
        {
          path: "settings",
          name: "settings",
          component: () => import("@/views/settings/index.vue"),
          meta: {
            title: NAV_ITEMS[5].pageTitle,
            roles: NAV_ITEMS[5].roles,
            permissions: NAV_ITEMS[5].permissions,
          },
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
  document.title = `${(to.meta.title as string) || "控制台"} · ApiHub`;

  const token = getToken();

  if (to.meta.public) {
    if (token && to.path === "/login") {
      return "/dashboard";
    }
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

  const roles = userStore.roles;
  const permissions = userStore.permissions;

  if (to.meta.roles && !hasRole(roles, to.meta.roles)) {
    return { path: "/403", query: { from: to.fullPath } };
  }

  if (to.meta.permissions && !hasPermission(permissions, to.meta.permissions)) {
    return { path: "/403", query: { from: to.fullPath } };
  }

  return true;
});

export default router;
