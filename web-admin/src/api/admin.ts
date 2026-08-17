import http from "@/api/http";
import { enrichInterface, enrichLog, mockApps, mockAnalytics, mockInterfaces, mockLogs, mockOverview } from "@/api/mock";
import type { ApiApp, ApiInterface, ApiResult, AnalyticsStat, InvokeLog, OverviewStat } from "@/types";

/** 优先真实接口，失败时回退演示数据 */
export async function fetchInterfaces(): Promise<ApiInterface[]> {
  try {
    const res = await http.get<ApiResult<ApiInterface[]>>("/admin/interfaces");
    const list = res.data.data || [];
    return list.map(enrichInterface);
  } catch {
    return mockInterfaces;
  }
}

export async function fetchInterfaceById(id: number): Promise<ApiInterface | null> {
  const list = await fetchInterfaces();
  return list.find((item) => item.id === id) || null;
}

export async function fetchOverview(): Promise<OverviewStat> {
  return Promise.resolve(mockOverview);
}

export async function fetchAnalytics(): Promise<AnalyticsStat> {
  return Promise.resolve(mockAnalytics);
}

export async function fetchApps(): Promise<ApiApp[]> {
  return Promise.resolve(mockApps);
}

export async function fetchLogs(): Promise<InvokeLog[]> {
  return Promise.resolve(mockLogs);
}

export async function fetchLogByTraceId(traceId: string): Promise<InvokeLog | null> {
  const list = await fetchLogs();
  return list.find((item) => item.traceId === traceId) || null;
}
