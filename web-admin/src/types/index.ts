export interface ApiResult<T> {
  code: number;
  msg: string;
  data: T;
  traceId?: string;
}

export interface LoginVO {
  token: string;
  userId: number;
  username: string;
  nickname?: string;
  roles: string[];
  permissions?: string[];
}

/** HTTP 请求方法 */
export type HttpMethod = "GET" | "POST" | "PUT" | "DELETE" | "PATCH";

/** 接口上下线状态 */
export type ApiStatus = 0 | 1;

export interface ApiParam {
  name: string;
  location: "query" | "header" | "body";
  type: string;
  required: boolean;
  desc: string;
}

export interface ApiInterface {
  id: number;
  name: string;
  path: string;
  method: HttpMethod | string;
  description?: string;
  version: string;
  category?: string;
  status: ApiStatus | number;
  /** @deprecated 后端 snake_case，前端统一使用 createTime */
  create_time?: string;
  createTime?: string;
  callCount?: number;
  owner?: string;
  authType?: string;
  params?: ApiParam[];
  responseExample?: string;
}

export interface ApiApp {
  id: number;
  appId: string;
  appName: string;
  description?: string;
  appSecret?: string;
  status: number;
  qpsLimit: number;
  dailyQuota: number;
  interfaceCount?: number;
  invokeCount?: number;
  owner?: string;
  createTime: string;
}

export interface TraceSpan {
  name: string;
  service: string;
  costMs: number;
  status: "success" | "warning" | "error";
}

export interface DurationSlice {
  label: string;
  ms: number;
  percent: number;
}

export interface InvokeLog {
  id: number;
  traceId: string;
  appId: string;
  path: string;
  name?: string;
  method: string;
  statusCode: number;
  costMs: number;
  ip: string;
  createTime: string;
  callerName?: string;
  requestParams?: Record<string, unknown>;
  responseBody?: Record<string, unknown>;
  spans?: TraceSpan[];
  durationBreakdown?: DurationSlice[];
}

export interface OverviewStat {
  todayCalls: number;
  successRate: number;
  onlineInterfaces: number;
  activeApps: number;
  apiCount: number;
  apiTrend: string;
  callTrendPct: string;
  successTrend: string;
  appTrend: string;
  callTrend: number[];
  trendLabels: string[];
  topInterfaces: { name: string; value: number }[];
}

export interface AnalyticsStat {
  todayCalls: number;
  successRate: number;
  avgLatencyMs: number;
  p95LatencyMs: number;
  callTrendPct: string;
  successTrend: string;
  latencyTrend: string;
  callTrend: number[];
  trendLabels: string[];
  topInterfaces: { name: string; value: number }[];
  statusRatio: { name: string; value: number }[];
  latencyByInterface: { name: string; avgMs: number; p95Ms: number }[];
  topApps: { name: string; value: number }[];
}

/** 面包屑导航项 */
export interface BreadcrumbItem {
  title: string;
  path?: string;
}
