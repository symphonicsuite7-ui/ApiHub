import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

/**
 * 开发与生产统一使用相对路径 /api。
 * 开发：Vite 代理到网关 8080；
 * 生产：由 Nginx 把 /api 反代到网关，静态资源走 dist。
 */
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://127.0.0.1:8080",
        changeOrigin: true,
      },
    },
  },
});
