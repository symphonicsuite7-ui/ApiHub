<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { Histogram, Monitor, Odometer, Timer } from "@element-plus/icons-vue";
import { fetchAnalytics } from "@/api/admin";
import StatCard from "@/components/StatCard.vue";
import type { AnalyticsStat } from "@/types";

const loading = ref(true);
const data = ref<AnalyticsStat | null>(null);
const nowText = ref("");

const trendRef = ref<HTMLDivElement | null>(null);
const pieRef = ref<HTMLDivElement | null>(null);
const apiRankRef = ref<HTMLDivElement | null>(null);
const appRankRef = ref<HTMLDivElement | null>(null);
const latencyRef = ref<HTMLDivElement | null>(null);

let charts: echarts.ECharts[] = [];
let clockTimer: number | null = null;

const tooltipBase = {
  backgroundColor: "rgba(15, 23, 42, 0.92)",
  borderColor: "#334155",
  textStyle: { color: "#f8fafc", fontSize: 12 },
};

function tickClock() {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  nowText.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

function initChart(el: HTMLDivElement | null, option: echarts.EChartsOption) {
  if (!el) return;
  const chart = echarts.init(el);
  chart.setOption(option);
  charts.push(chart);
}

function renderCharts(stat: AnalyticsStat) {
  charts.forEach((c) => c.dispose());
  charts = [];

  initChart(trendRef.value, {
    backgroundColor: "transparent",
    tooltip: { ...tooltipBase, trigger: "axis" },
    grid: { left: 52, right: 20, top: 36, bottom: 32 },
    xAxis: {
      type: "category",
      data: stat.trendLabels,
      boundaryGap: false,
      axisLine: { lineStyle: { color: "#1f2937" } },
      axisLabel: { color: "#94a3b8" },
      axisTick: { show: false },
    },
    yAxis: {
      type: "value",
      splitLine: { lineStyle: { color: "#1f2937", type: "dashed" } },
      axisLabel: { color: "#94a3b8" },
    },
    series: [
      {
        name: "调用量",
        type: "line",
        smooth: true,
        data: stat.callTrend,
        symbol: "circle",
        symbolSize: 8,
        itemStyle: { color: "#38bdf8", borderColor: "#0f172a", borderWidth: 2 },
        lineStyle: { color: "#2563eb", width: 3 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(37, 99, 235, 0.45)" },
            { offset: 1, color: "rgba(37, 99, 235, 0.02)" },
          ]),
        },
      },
    ],
  });

  initChart(pieRef.value, {
    backgroundColor: "transparent",
    tooltip: { ...tooltipBase, trigger: "item", formatter: "{b}: {c} ({d}%)" },
    legend: {
      bottom: 4,
      itemWidth: 10,
      itemHeight: 10,
      textStyle: { color: "#94a3b8", fontSize: 12 },
    },
    series: [
      {
        type: "pie",
        radius: ["42%", "68%"],
        center: ["50%", "44%"],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: "#111827", borderWidth: 2 },
        label: { color: "#cbd5e1", fontSize: 12 },
        labelLine: { lineStyle: { color: "#475569" } },
        data: stat.statusRatio.map((item, i) => ({
          name: item.name,
          value: item.value,
          itemStyle: {
            color: i === 0 ? "#22c55e" : i === 1 ? "#f59e0b" : "#ef4444",
          },
        })),
      },
    ],
  });

  const apiNames = stat.topInterfaces.map((i) => i.name);
  const apiValues = stat.topInterfaces.map((i) => i.value);

  initChart(apiRankRef.value, {
    backgroundColor: "transparent",
    tooltip: { ...tooltipBase, trigger: "axis", axisPointer: { type: "shadow" } },
    grid: { left: 100, right: 24, top: 16, bottom: 24 },
    xAxis: {
      type: "value",
      splitLine: { lineStyle: { color: "#1f2937", type: "dashed" } },
      axisLabel: { color: "#94a3b8" },
    },
    yAxis: {
      type: "category",
      data: apiNames,
      inverse: true,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: "#cbd5e1", fontSize: 12 },
    },
    series: [
      {
        name: "调用量",
        type: "bar",
        data: apiValues,
        barWidth: 14,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: "#1d4ed8" },
            { offset: 1, color: "#38bdf8" },
          ]),
        },
        label: {
          show: true,
          position: "right",
          color: "#94a3b8",
          fontSize: 11,
          formatter: "{c}",
        },
      },
    ],
  });

  const appNames = stat.topApps.map((i) => i.name);
  const appValues = stat.topApps.map((i) => i.value);

  initChart(appRankRef.value, {
    backgroundColor: "transparent",
    tooltip: { ...tooltipBase, trigger: "axis", axisPointer: { type: "shadow" } },
    grid: { left: 100, right: 24, top: 16, bottom: 24 },
    xAxis: {
      type: "value",
      splitLine: { lineStyle: { color: "#1f2937", type: "dashed" } },
      axisLabel: { color: "#94a3b8" },
    },
    yAxis: {
      type: "category",
      data: appNames,
      inverse: true,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: "#cbd5e1", fontSize: 12 },
    },
    series: [
      {
        name: "调用量",
        type: "bar",
        data: appValues,
        barWidth: 14,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: "#6d28d9" },
            { offset: 1, color: "#a78bfa" },
          ]),
        },
        label: {
          show: true,
          position: "right",
          color: "#94a3b8",
          fontSize: 11,
          formatter: "{c}",
        },
      },
    ],
  });

  const latencyNames = stat.latencyByInterface.map((i) => i.name);

  initChart(latencyRef.value, {
    backgroundColor: "transparent",
    tooltip: { ...tooltipBase, trigger: "axis", axisPointer: { type: "shadow" } },
    legend: {
      top: 4,
      right: 12,
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { color: "#94a3b8" },
    },
    grid: { left: 48, right: 20, top: 44, bottom: 36 },
    xAxis: {
      type: "category",
      data: latencyNames,
      axisLine: { lineStyle: { color: "#1f2937" } },
      axisLabel: { color: "#94a3b8", fontSize: 11, rotate: 20 },
      axisTick: { show: false },
    },
    yAxis: {
      type: "value",
      name: "ms",
      nameTextStyle: { color: "#64748b", fontSize: 11 },
      splitLine: { lineStyle: { color: "#1f2937", type: "dashed" } },
      axisLabel: { color: "#94a3b8" },
    },
    series: [
      {
        name: "平均耗时",
        type: "bar",
        barGap: "30%",
        barWidth: 18,
        data: stat.latencyByInterface.map((i) => i.avgMs),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#38bdf8" },
            { offset: 1, color: "#1d4ed8" },
          ]),
        },
      },
      {
        name: "P95 耗时",
        type: "bar",
        barWidth: 18,
        data: stat.latencyByInterface.map((i) => i.p95Ms),
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "#f59e0b" },
            { offset: 1, color: "#b45309" },
          ]),
        },
      },
    ],
  });
}

function resize() {
  charts.forEach((c) => c.resize());
}

onMounted(async () => {
  tickClock();
  clockTimer = window.setInterval(tickClock, 1000);
  data.value = await fetchAnalytics();
  loading.value = false;
  requestAnimationFrame(() => {
    if (data.value) renderCharts(data.value);
  });
  window.addEventListener("resize", resize);
});

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer);
  window.removeEventListener("resize", resize);
  charts.forEach((c) => c.dispose());
  charts = [];
});
</script>

<template>
  <div class="page analytics-screen" v-loading="loading">
    <!-- 大屏顶栏 -->
    <header class="screen-hero">
      <div class="hero-left">
        <div class="hero-badge">MONITORING</div>
        <h1>ApiHub 数据分析大屏</h1>
        <p>企业级 API 调用监控 · 实时趋势 · 质量分析</p>
      </div>
      <div class="hero-right">
        <div class="live-status">
          <span class="pulse" />
          <span>实时监控中</span>
        </div>
        <time class="clock">{{ nowText }}</time>
      </div>
      <div class="hero-grid" />
      <div class="hero-glow" />
    </header>

    <!-- 核心指标 -->
    <div class="stat-grid" v-if="data">
      <StatCard
        label="今日调用量"
        :value="data.todayCalls.toLocaleString()"
        :trend="data.callTrendPct"
        :icon="Histogram"
        tone="cyan"
      />
      <StatCard
        label="调用成功率"
        :value="data.successRate.toFixed(2) + '%'"
        :trend="data.successTrend"
        :icon="Monitor"
        tone="green"
      />
      <StatCard
        label="平均响应耗时"
        :value="data.avgLatencyMs + ' ms'"
        :trend="data.latencyTrend"
        :up="false"
        :icon="Timer"
        tone="blue"
      />
      <StatCard label="P95 响应耗时" :value="data.p95LatencyMs + ' ms'" :icon="Odometer" tone="violet" />
    </div>

    <!-- 第一行：趋势 + 状态比例 -->
    <div class="screen-row row-main">
      <section class="panel panel-lg">
        <div class="panel-corner tl" /><div class="panel-corner tr" />
        <div class="panel-corner bl" /><div class="panel-corner br" />
        <div class="panel-head">
          <div class="panel-title">
            <i class="dot cyan" />
            <h3>接口调用趋势</h3>
          </div>
          <span class="panel-sub">近 7 日调用量折线图</span>
        </div>
        <div ref="trendRef" class="chart chart-lg" />
      </section>

      <section class="panel panel-sm">
        <div class="panel-corner tl" /><div class="panel-corner tr" />
        <div class="panel-corner bl" /><div class="panel-corner br" />
        <div class="panel-head">
          <div class="panel-title">
            <i class="dot green" />
            <h3>调用状态比例</h3>
          </div>
          <span class="panel-sub">成功 / 失败 / 异常</span>
        </div>
        <div ref="pieRef" class="chart chart-md" />
        <div class="status-legend" v-if="data">
          <div v-for="(item, idx) in data.statusRatio" :key="item.name" class="legend-item">
            <i :class="'c' + idx" />
            <span>{{ item.name }}</span>
            <strong>{{ item.value.toLocaleString() }}</strong>
          </div>
        </div>
      </section>
    </div>

    <!-- 第二行：接口排行 + 应用排行 -->
    <div class="screen-row row-half">
      <section class="panel">
        <div class="panel-corner tl" /><div class="panel-corner tr" />
        <div class="panel-corner bl" /><div class="panel-corner br" />
        <div class="panel-head">
          <div class="panel-title">
            <i class="dot blue" />
            <h3>接口访问排行</h3>
          </div>
          <span class="panel-sub">今日调用量 Top</span>
        </div>
        <div ref="apiRankRef" class="chart chart-rank" />
      </section>

      <section class="panel">
        <div class="panel-corner tl" /><div class="panel-corner tr" />
        <div class="panel-corner bl" /><div class="panel-corner br" />
        <div class="panel-head">
          <div class="panel-title">
            <i class="dot violet" />
            <h3>应用调用排行</h3>
          </div>
          <span class="panel-sub">按 App 聚合调用量</span>
        </div>
        <div ref="appRankRef" class="chart chart-rank" />
      </section>
    </div>

    <!-- 第三行：响应耗时 -->
    <section class="panel panel-full">
      <div class="panel-corner tl" /><div class="panel-corner tr" />
      <div class="panel-corner bl" /><div class="panel-corner br" />
      <div class="panel-head row-head">
        <div>
          <div class="panel-title">
            <i class="dot amber" />
            <h3>接口响应耗时</h3>
          </div>
          <span class="panel-sub">平均耗时 vs P95 耗时对比</span>
        </div>
        <div class="latency-tags" v-if="data">
          <span class="tag avg">平均 {{ data.avgLatencyMs }} ms</span>
          <span class="tag p95">P95 {{ data.p95LatencyMs }} ms</span>
        </div>
      </div>
      <div ref="latencyRef" class="chart chart-latency" />
    </section>
  </div>
</template>

<style scoped>
.analytics-screen {
  max-width: none;
  padding: 20px 24px 36px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 大屏顶栏 */
.screen-hero {
  position: relative;
  padding: 22px 28px;
  border: 1px solid rgba(56, 189, 248, 0.22);
  border-radius: 14px;
  background:
  linear-gradient(135deg, rgba(37, 99, 235, 0.14) 0%, transparent 50%),
  linear-gradient(225deg, rgba(56, 189, 248, 0.08) 0%, transparent 45%),
  var(--bg-elevated);
  overflow: hidden;
}

.hero-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(56, 189, 248, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(56, 189, 248, 0.04) 1px, transparent 1px);
  background-size: 32px 32px;
  pointer-events: none;
}

.hero-glow {
  position: absolute;
  top: -60%;
  right: -10%;
  width: 420px;
  height: 420px;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.18), transparent 65%);
  pointer-events: none;
}

.hero-left,
.hero-right {
  position: relative;
  z-index: 1;
}

.screen-hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.hero-badge {
  display: inline-block;
  padding: 3px 10px;
  margin-bottom: 10px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 2px;
  color: #38bdf8;
  border: 1px solid rgba(56, 189, 248, 0.35);
  border-radius: 4px;
  background: rgba(56, 189, 248, 0.08);
}

.hero-left h1 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 700;
  letter-spacing: 1px;
}

.hero-left p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.hero-right {
  text-align: right;
}

.live-status {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  font-size: 13px;
  color: #4ade80;
  margin-bottom: 8px;
}

.pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.55);
  animation: pulse 1.8s infinite;
}

.clock {
  display: block;
  font-size: 20px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  color: var(--accent);
  letter-spacing: 1px;
}

/* 面板通用 */
.panel {
  position: relative;
  padding: 16px 18px 12px;
  border: 1px solid rgba(51, 65, 85, 0.8);
  border-radius: 12px;
  background: rgba(17, 24, 39, 0.85);
  box-shadow: inset 0 1px 0 rgba(148, 163, 184, 0.06);
}

.panel-corner {
  position: absolute;
  width: 12px;
  height: 12px;
  border-color: rgba(56, 189, 248, 0.5);
  border-style: solid;
  pointer-events: none;
}

.panel-corner.tl {
  top: 0;
  left: 0;
  border-width: 2px 0 0 2px;
}
.panel-corner.tr {
  top: 0;
  right: 0;
  border-width: 2px 2px 0 0;
}
.panel-corner.bl {
  bottom: 0;
  left: 0;
  border-width: 0 0 2px 2px;
}
.panel-corner.br {
  bottom: 0;
  right: 0;
  border-width: 0 2px 2px 0;
}

.panel-head {
  margin-bottom: 8px;
}

.panel-head.row-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
}

.panel-title h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.dot.cyan { background: #38bdf8; box-shadow: 0 0 8px rgba(56, 189, 248, 0.6); }
.dot.green { background: #22c55e; box-shadow: 0 0 8px rgba(34, 197, 94, 0.6); }
.dot.blue { background: #3b82f6; box-shadow: 0 0 8px rgba(59, 130, 246, 0.6); }
.dot.violet { background: #a78bfa; box-shadow: 0 0 8px rgba(167, 139, 250, 0.6); }
.dot.amber { background: #f59e0b; box-shadow: 0 0 8px rgba(245, 158, 11, 0.6); }

.panel-sub {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-muted);
}

/* 布局行 */
.screen-row {
  display: grid;
  gap: 16px;
}

.row-main {
  grid-template-columns: 1.65fr 1fr;
}

.row-half {
  grid-template-columns: 1fr 1fr;
}

.panel-full {
  margin-top: 0;
}

/* 图表尺寸 */
.chart {
  width: 100%;
}

.chart-lg {
  height: 300px;
}

.chart-md {
  height: 220px;
}

.chart-rank {
  height: 280px;
}

.chart-latency {
  height: 300px;
}

/* 状态图例 */
.status-legend {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 4px 0 8px;
  border-top: 1px solid var(--border);
  margin-top: 4px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}

.legend-item i {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.legend-item i.c0 { background: #22c55e; }
.legend-item i.c1 { background: #f59e0b; }
.legend-item i.c2 { background: #ef4444; }

.legend-item strong {
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

/* 耗时标签 */
.latency-tags {
  display: flex;
  gap: 10px;
}

.tag {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

.tag.avg {
  color: #38bdf8;
  background: rgba(56, 189, 248, 0.12);
  border: 1px solid rgba(56, 189, 248, 0.25);
}

.tag.p95 {
  color: #f59e0b;
  background: rgba(245, 158, 11, 0.12);
  border: 1px solid rgba(245, 158, 11, 0.25);
}

@keyframes pulse {
  70% {
    box-shadow: 0 0 0 8px rgba(34, 197, 94, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(34, 197, 94, 0);
  }
}

@media (max-width: 1100px) {
  .row-main,
  .row-half {
    grid-template-columns: 1fr;
  }

  .screen-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .hero-right {
    text-align: left;
  }

  .live-status {
    justify-content: flex-start;
  }
}
</style>
