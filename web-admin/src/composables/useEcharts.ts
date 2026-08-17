import * as echarts from "echarts";
import type { EChartsOption } from "echarts";
import { onBeforeUnmount, onMounted } from "vue";

/** 暗色主题图表色板 */
export const chartPalette = {
  primary: "#2563eb",
  accent: "#38bdf8",
  success: "#22c55e",
  warning: "#f59e0b",
  danger: "#ef4444",
  violet: "#a78bfa",
  axis: "#94a3b8",
  splitLine: "#1f2937",
  border: "#334155",
  tooltipBg: "rgba(15, 23, 42, 0.92)",
};

/** 暗色 Tooltip 基础配置 */
export function darkTooltip(trigger: "axis" | "item" = "axis") {
  return {
    trigger,
    backgroundColor: chartPalette.tooltipBg,
    borderColor: chartPalette.border,
    textStyle: { color: "#f8fafc", fontSize: 12 },
  };
}

/** 暗色类目轴 */
export function darkCategoryAxis(data: string[], boundaryGap = false) {
  return {
    type: "category" as const,
    data,
    boundaryGap,
    axisLine: { lineStyle: { color: chartPalette.splitLine } },
    axisLabel: { color: chartPalette.axis },
    axisTick: { show: false },
  };
}

/** 暗色数值轴 */
export function darkValueAxis(name?: string) {
  return {
    type: "value" as const,
    name,
    nameTextStyle: { color: "#64748b", fontSize: 11 },
    splitLine: { lineStyle: { color: chartPalette.splitLine, type: "dashed" as const } },
    axisLabel: { color: chartPalette.axis },
  };
}

/** 管理多实例 ECharts 生命周期 */
export function useEcharts() {
  const instances: echarts.ECharts[] = [];

  function mount(el: HTMLElement | null, option: EChartsOption) {
    if (!el) return null;
    const chart = echarts.init(el);
    chart.setOption(option);
    instances.push(chart);
    return chart;
  }

  function disposeAll() {
    instances.forEach((chart) => chart.dispose());
    instances.length = 0;
  }

  function resizeAll() {
    instances.forEach((chart) => chart.resize());
  }

  onMounted(() => window.addEventListener("resize", resizeAll));
  onBeforeUnmount(() => {
    window.removeEventListener("resize", resizeAll);
    disposeAll();
  });

  return { mount, disposeAll, resizeAll };
}

/** 折线图区域渐变 */
export function lineAreaGradient() {
  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
    { offset: 0, color: "rgba(37, 99, 235, 0.45)" },
    { offset: 1, color: "rgba(37, 99, 235, 0.02)" },
  ]);
}
