import http, { isApiResult } from "@/api/http";
import { enrichInterface, enrichLog, mockApps, mockAnalytics, mockInterfaces, mockLogs, mockOverview } from "@/api/mock";
import type { ApiApp, ApiInterface, ApiResult, AnalyticsStat, InvokeLog, OverviewStat } from "@/types";

/** 真实接口优先，失败回退 mock（便于演示与联调过渡） */
async function fetchWithMockFallback<T>(fetcher: () => Promise<T>, fallback: T): Promise<T> {
  try {
    return await fetcher();
  } catch {
    return fallback;
  }
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
    return isApiResult(res.data) ? res.data.data || [] : mockApps;
  }, mockApps);
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
