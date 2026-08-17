import { createRouter, createWebHistory } from "vue-router";
import { getToken } from "@/utils/auth";

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
      component: () => import("@/layouts/ConsoleLayout.vue"),
      redirect: "/dashboard",
      children: [
        {
          path: "dashboard",
          name: "dashboard",
          component: () => import("@/views/dashboard/index.vue"),
          meta: { title: "Dashboard" },
        },
        {
          path: "interfaces",
          name: "interfaces",
          component: () => import("@/views/interfaces/index.vue"),
          meta: { title: "接口资产中心" },
        },
        {
          path: "interfaces/:id",
          name: "interface-detail",
          component: () => import("@/views/interfaces/detail.vue"),
          meta: { title: "接口详情" },
        },
        {
          path: "apps",
          name: "apps",
          component: () => import("@/views/apps/index.vue"),
          meta: { title: "应用中心" },
        },
        {
          path: "logs",
          name: "logs",
          component: () => import("@/views/logs/index.vue"),
          meta: { title: "调用日志" },
        },
        {
          path: "stats",
          name: "stats",
          component: () => import("@/views/stats/index.vue"),
          meta: { title: "数据统计" },
        },
        {
          path: "settings",
          name: "settings",
          component: () => import("@/views/settings/index.vue"),
          meta: { title: "系统设置" },
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
  return true;
});

export default router;
