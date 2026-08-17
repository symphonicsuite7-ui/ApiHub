import http from "@/api/http";
import type { ApiResult, LoginVO } from "@/types";

export function register(payload: { username: string; password: string; nickname?: string }) {
  return http.post<ApiResult<void>>("/auth/register", payload);
}

export function login(payload: { username: string; password: string }) {
  return http.post<ApiResult<LoginVO>>("/auth/login", payload);
}

export function fetchMe() {
  return http.get<ApiResult<LoginVO>>("/auth/me");
}
