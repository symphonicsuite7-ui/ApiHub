import http, { isApiResult } from "@/api/http";
import { enrichInterface, enrichLog, mockApps, mockAnalytics, mockInterfaces, mockLogs, mockOverview } from "@/api/mock";
import type { ApiApp, AppDetail, ApiInterface, ApiResult, AnalyticsStat, InvokeLog, OverviewStat } from "@/types";

/** 真实接口优先，失败回退 mock（便于演示与联调过渡） */
async function fetchWithMockFallback<T>(fetcher: () => Promise<T>, fallback: T): Promise<T> {
  try {
    return await fetcher();
  } catch {
    return fallback;
  }
}

/** 后端 LocalDateTime 序列化为 "2026-08-21T15:30:00"，统一展示为 "2026-08-21 15:30" */
function formatTime(value?: string): string {
  if (!value) return "";
  return value.includes("T") ? value.replace("T", " ").slice(0, 16) : value.slice(0, 16);
}

/** 补齐后端 AppVO 缺失的前端展示字段（description/interfaceCount/invokeCount/owner） */
function enrichApp(raw: ApiApp): ApiApp {
  return {
    ...raw,
    description: raw.description || "",
    interfaceCount: raw.interfaceCount ?? 0,
    invokeCount: raw.invokeCount ?? 0,
    owner: raw.owner || "Admin",
    createTime: formatTime(raw.createTime),
  };
}

/** 后端不可用时的本地演示创建（与原页面行为一致） */
function makeMockApp(appName: string, qpsLimit: number, dailyQuota: number): ApiApp {
  const stamp = Date.now().toString(36).slice(-8);
  return {
    id: Date.now(),
    appId: `app_${stamp}`,
    appName,
    description: "",
    appSecret: `${stamp}${Math.random().toString(36).slice(2, 8)}`,
    status: 1,
    qpsLimit,
    dailyQuota,
    interfaceCount: 0,
    invokeCount: 0,
    owner: "Current User",
    createTime: new Date().toLocaleString("zh-CN", { hour12: false }),
  };
}

export async function fetchInterfaces(): Promise<ApiInterface[]> {
  return fetchWithMockFallback(async () => {
    const res = await http.get<ApiResult<ApiInterface[]>>("/admin/interfaces");
    const body = res.data;
    const list = isApiResult(body) ? body.data || [] : [];
    return list.map(enrichInterface);
  }, mockInterfaces);
}

export async function fetchInterfaceById(id: number): Promise<ApiInterface | null> {
  if (!Number.isFinite(id) || id <= 0) return null;
  const list = await fetchInterfaces();
  return list.find((item) => item.id === id) || null;
}

export async function fetchOverview(): Promise<OverviewStat> {
  return fetchWithMockFallback(async () => {
    const res = await http.get<ApiResult<OverviewStat>>("/admin/overview");
    return isApiResult(res.data) ? res.data.data : mockOverview;
  }, mockOverview);
}

export async function fetchAnalytics(): Promise<AnalyticsStat> {
  return fetchWithMockFallback(async () => {
    const res = await http.get<ApiResult<AnalyticsStat>>("/admin/analytics");
    return isApiResult(res.data) ? res.data.data : mockAnalytics;
  }, mockAnalytics);
}

export async function fetchApps(): Promise<ApiApp[]> {
  return fetchWithMockFallback(async () => {
    const res = await http.get<ApiResult<ApiApp[]>>("/admin/apps");
    const body = res.data;
    const list = isApiResult(body) ? body.data || [] : [];
    return list.map(enrichApp);
  }, mockApps.map(enrichApp));
}

/**
 * 创建应用。真实接口失败时回退本地演示数据（页面标记为演示模式）。
 * 成功后返回含 AppSecret 的完整信息（仅创建响应携带 Secret）。
 */
export async function createApp(payload: {
  appName: string;
  qpsLimit?: number;
  dailyQuota?: number;
}): Promise<{ app: ApiApp; demo: boolean }> {
  const fallback = makeMockApp(payload.appName, payload.qpsLimit || 10, payload.dailyQuota || 1000);
  try {
    const res = await http.post<ApiResult<ApiApp>>("/admin/apps", payload);
    const body = res.data;
    if (!isApiResult(body) || !body.data) throw new Error("响应格式异常");
    return { app: enrichApp(body.data), demo: false };
  } catch {
    return { app: fallback, demo: true };
  }
}

/** 启用/禁用应用。写操作为真实调用，失败抛错由页面提示。 */
export async function updateAppStatus(id: number, status: number): Promise<void> {
  await http.put(`/admin/apps/${id}/status`, { status });
}

export async function fetchAppDetail(id: number): Promise<AppDetail> {
  const res = await http.get<ApiResult<AppDetail>>(`/admin/apps/${id}`);
  const body = res.data;
  if (!isApiResult(body) || !body.data) throw new Error("响应格式异常");
  const detail = body.data;
  return { ...enrichApp(detail), grantedInterfaces: detail.grantedInterfaces || [] };
}

/** 为应用批量开通接口 */
export async function grantInterfaces(appId: string, interfaceIds: number[]): Promise<void> {
  await http.post(`/admin/apps/${appId}/grants`, { interfaceIds });
}

/** 取消开通单个接口 */
export async function revokeInterface(appId: string, interfaceId: number): Promise<void> {
  await http.delete(`/admin/apps/${appId}/grants/${interfaceId}`);
}

export async function fetchLogs(): Promise<InvokeLog[]> {
  return fetchWithMockFallback(async () => {
    const res = await http.get<ApiResult<InvokeLog[]>>("/admin/logs");
    const list = isApiResult(res.data) ? res.data.data || [] : [];
    return list.map(enrichLog);
  }, mockLogs);
}

export async function fetchLogByTraceId(traceId: string): Promise<InvokeLog | null> {
  const list = await fetchLogs();
  return list.find((item) => item.traceId === traceId) || null;
}
