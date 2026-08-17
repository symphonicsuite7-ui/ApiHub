import axios from "axios";
import router from "@/router";
import { clearToken, getToken } from "@/utils/auth";

/** 开发走 Vite 代理，生产走 Nginx 反代，均使用相对路径 /api */
const http = axios.create({
  baseURL: "/api",
  timeout: 15000,
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const body = response.data;
    if (body && typeof body.code === "number" && body.code !== 0) {
      const msg = body.msg || "请求失败";
      if (body.code === 401) {
        clearToken();
        if (router.currentRoute.value.path !== "/login") {
          router.push("/login");
        }
      }
      return Promise.reject(new Error(msg));
    }
    return response;
  },
  (error) => {
    const status = error.response?.status;
    if (status === 401) {
      clearToken();
      if (router.currentRoute.value.path !== "/login") {
        router.push("/login");
      }
    }
    const msg = error.response?.data?.msg || error.message || "网络异常";
    return Promise.reject(new Error(msg));
  }
);

export default http;
