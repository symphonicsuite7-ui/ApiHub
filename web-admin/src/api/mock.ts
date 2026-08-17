import type { ApiInterface, ApiApp, InvokeLog, OverviewStat } from "@/types";

/** 按路径补齐 Marketplace 展示字段 */
export const interfaceExtras: Record<string, Partial<ApiInterface>> = {
  "/api/open/weather": {
    name: "Weather API",
    callCount: 12000,
    owner: "Admin",
    authType: "AppId + HMAC-SHA256 签名",
    params: [
      { name: "city", location: "query", type: "string", required: true, desc: "城市名称，例如 北京" },
    ],
    responseExample: JSON.stringify(
      {
        code: 0,
        msg: "success",
        data: { city: "北京", weather: "晴", temperature: "26℃" },
        traceId: "7c1e9a2b4d8f01",
      },
      null,
      2
    ),
  },
  "/api/open/health": {
    name: "Health API",
    callCount: 1860,
    owner: "Admin",
    authType: "网关放行",
    params: [],
    responseExample: JSON.stringify(
      { code: 0, msg: "success", data: { service: "api-invoke", status: "UP" } },
      null,
      2
    ),
  },
  "/api/open/sms/send": {
    name: "SMS API",
    callCount: 1860,
    owner: "Admin",
    authType: "AppId + HMAC-SHA256 签名",
    params: [
      { name: "phone", location: "body", type: "string", required: true, desc: "手机号" },
      { name: "content", location: "body", type: "string", required: true, desc: "短信内容" },
    ],
    responseExample: JSON.stringify(
      { code: 0, msg: "success", data: { messageId: "sms_9921", status: "queued" } },
      null,
      2
    ),
  },
  "/api/open/translate": {
    name: "Translate API",
    callCount: 3104,
    owner: "Admin",
    authType: "AppId + HMAC-SHA256 签名",
    params: [
      { name: "text", location: "body", type: "string", required: true, desc: "待翻译文本" },
      { name: "from", location: "body", type: "string", required: false, desc: "源语言，默认 auto" },
      { name: "to", location: "body", type: "string", required: true, desc: "目标语言" },
    ],
    responseExample: JSON.stringify(
      { code: 0, msg: "success", data: { text: "Hello", translated: "你好", to: "zh" } },
      null,
      2
    ),
  },
};

export function enrichInterface(item: ApiInterface): ApiInterface {
  const extra = interfaceExtras[item.path] || {};
  return {
    callCount: 0,
    owner: "Admin",
    authType: "AppId + HMAC-SHA256 签名",
    params: [],
    responseExample: JSON.stringify({ code: 0, msg: "success", data: {} }, null, 2),
    ...item,
    ...extra,
    name: extra.name || item.name,
    createTime: item.createTime || item.create_time || "",
  };
}

/** 无后端接口时的演示数据，后续可无缝替换为真实 API */
export const mockInterfaces: ApiInterface[] = [
  {
    id: 1,
    name: "Weather API",
    path: "/api/open/weather",
    method: "GET",
    description: "按城市查询实时天气，适合开放给业务系统调用",
    version: "v1",
    category: "生活服务",
    status: 1,
    createTime: "2026-08-17 14:23:33",
  },
  {
    id: 2,
    name: "Health API",
    path: "/api/open/health",
    method: "GET",
    description: "开放服务健康检查",
    version: "v1",
    category: "系统",
    status: 1,
    createTime: "2026-08-17 14:23:33",
  },
  {
    id: 3,
    name: "SMS API",
    path: "/api/open/sms/send",
    method: "POST",
    description: "短信发送模拟接口，用于通知与验证码场景",
    version: "v1",
    category: "通信",
    status: 1,
    createTime: "2026-08-16 09:12:00",
  },
  {
    id: 4,
    name: "Translate API",
    path: "/api/open/translate",
    method: "POST",
    description: "多语言翻译模拟接口",
    version: "v1",
    category: "AI",
    status: 1,
    createTime: "2026-08-15 18:40:21",
  },
].map(enrichInterface);

export const mockApps: ApiApp[] = [
  {
    id: 1,
    appId: "app_8f2a91c3",
    appName: "开放网关演示",
    status: 1,
    qpsLimit: 20,
    dailyQuota: 5000,
    createTime: "2026-08-12 10:21:00",
  },
  {
    id: 2,
    appId: "app_3b70e1aa",
    appName: "内部运营系统",
    status: 1,
    qpsLimit: 50,
    dailyQuota: 20000,
    createTime: "2026-08-10 16:08:00",
  },
  {
    id: 3,
    appId: "app_c91d44e0",
    appName: "测试沙箱",
    status: 0,
    qpsLimit: 5,
    dailyQuota: 200,
    createTime: "2026-08-08 11:30:00",
  },
];

export const mockLogs: InvokeLog[] = [
  {
    id: 101,
    traceId: "7c1e9a2b4d8f01",
    appId: "app_8f2a91c3",
    path: "/api/open/weather",
    name: "Weather API",
    method: "GET",
    statusCode: 200,
    costMs: 42,
    ip: "47.104.12.18",
    createTime: "2026-08-17 16:12:08",
  },
  {
    id: 102,
    traceId: "a91c33e80b1244",
    appId: "app_3b70e1aa",
    path: "/api/open/translate",
    name: "Translate API",
    method: "POST",
    statusCode: 200,
    costMs: 118,
    ip: "120.55.8.21",
    createTime: "2026-08-17 16:08:41",
  },
  {
    id: 103,
    traceId: "d4ee0192aa7703",
    appId: "app_8f2a91c3",
    path: "/api/open/sms/send",
    name: "SMS API",
    method: "POST",
    statusCode: 200,
    costMs: 86,
    ip: "47.104.12.18",
    createTime: "2026-08-17 16:05:02",
  },
  {
    id: 104,
    traceId: "bb2188f0c33119",
    appId: "app_3b70e1aa",
    path: "/api/open/weather",
    name: "Weather API",
    method: "GET",
    statusCode: 200,
    costMs: 37,
    ip: "36.110.20.9",
    createTime: "2026-08-17 15:59:19",
  },
  {
    id: 105,
    traceId: "e01c77aa992188",
    appId: "app_c91d44e0",
    path: "/api/open/translate",
    name: "Translate API",
    method: "POST",
    statusCode: 429,
    costMs: 8,
    ip: "127.0.0.1",
    createTime: "2026-08-17 15:51:50",
  },
  {
    id: 106,
    traceId: "f33a10c88d2201",
    appId: "app_8f2a91c3",
    path: "/api/open/sms/send",
    name: "SMS API",
    method: "POST",
    statusCode: 500,
    costMs: 214,
    ip: "112.80.15.6",
    createTime: "2026-08-17 15:44:27",
  },
];

export const mockOverview: OverviewStat = {
  todayCalls: 12546,
  successRate: 99.98,
  onlineInterfaces: 128,
  activeApps: 356,
  apiCount: 128,
  apiTrend: "+4.2%",
  callTrendPct: "+8.6%",
  successTrend: "+0.04%",
  appTrend: "+2.1%",
  callTrend: [8640, 9210, 10120, 10880, 11450, 11980, 12546],
  trendLabels: ["08-11", "08-12", "08-13", "08-14", "08-15", "08-16", "08-17"],
  topInterfaces: [
    { name: "Weather API", value: 4820 },
    { name: "Translate API", value: 3104 },
    { name: "SMS API", value: 1860 },
  ],
};
