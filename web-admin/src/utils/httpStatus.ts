import type { TraceSpan } from "@/types";

/** Element Plus Tag 类型 */
export type StatusTagType = "success" | "warning" | "danger" | "info";

/** HTTP 状态码 → Tag 类型 */
export function statusTagType(code: number): StatusTagType {
  if (code >= 200 && code < 300) return "success";
  if (code === 429) return "warning";
  return "danger";
}

/** HTTP 状态码 → 中文标签 */
export function statusLabel(code: number): string {
  if (code >= 200 && code < 300) return "成功";
  if (code === 429) return "限流";
  if (code >= 500) return "异常";
  return "失败";
}

/** 日志卡片左边框样式类 */
export function logBarClass(code: number): "bar-ok" | "bar-warn" | "bar-error" {
  if (code >= 500) return "bar-error";
  if (code === 429) return "bar-warn";
  return "bar-ok";
}

/** 链路 Span 状态色 */
export function spanStatusColor(status: TraceSpan["status"]): string {
  if (status === "error") return "var(--danger)";
  if (status === "warning") return "var(--warning)";
  return "var(--success)";
}
