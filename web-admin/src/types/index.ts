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
}

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
  method: string;
  description?: string;
  version: string;
  category?: string;
  status: number;
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
  status: number;
  qpsLimit: number;
  dailyQuota: number;
  createTime: string;
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
