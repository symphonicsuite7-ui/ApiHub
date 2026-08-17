<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { fetchOverview } from "@/api/admin";
import PageHeader from "@/components/PageHeader.vue";
import StatCard from "@/components/StatCard.vue";
import type { OverviewStat } from "@/types";

const loading = ref(true);
const data = ref<OverviewStat | null>(null);
const lineRef = ref<HTMLDivElement | null>(null);
const pieRef = ref<HTMLDivElement | null>(null);
let lineChart: echarts.ECharts | null = null;
let pieChart: echarts.ECharts | null = null;

onMounted(async () => {
  data.value = await fetchOverview();
  loading.value = false;
  requestAnimationFrame(() => {
    if (lineRef.value && data.value) {
      lineChart = echarts.init(lineRef.value);
      lineChart.setOption({
        backgroundColor: "transparent",
        tooltip: { trigger: "axis" },
        grid: { left: 40, right: 16, top: 24, bottom: 28 },
        xAxis: {
          type: "category",
          data: data.value.trendLabels,
          axisLabel: { color: "#94a3b8" },
          axisLine: { lineStyle: { color: "#1f2937" } },
        },
        yAxis: {
          type: "value",
          splitLine: { lineStyle: { color: "#1f2937" } },
          axisLabel: { color: "#94a3b8" },
        },
        series: [
          {
            name: "调用量",
            type: "line",
            smooth: true,
            data: data.value.callTrend,
            itemStyle: { color: "#38bdf8" },
            areaStyle: { color: "rgba(56, 189, 248, 0.12)" },
          },
        ],
      });
    }
    if (pieRef.value) {
      pieChart = echarts.init(pieRef.value);
      pieChart.setOption({
        backgroundColor: "transparent",
        tooltip: { trigger: "item" },
        legend: { bottom: 0, textStyle: { color: "#94a3b8" } },
        series: [
          {
            type: "pie",
            radius: ["46%", "68%"],
            label: { color: "#94a3b8" },
            data: [
              { name: "2xx", value: 11820, itemStyle: { color: "#22c55e" } },
              { name: "4xx", value: 740, itemStyle: { color: "#f59e0b" } },
              { name: "5xx", value: 280, itemStyle: { color: "#ef4444" } },
            ],
          },
        ],
      });
    }
  });
  window.addEventListener("resize", resize);
});

function resize() {
  lineChart?.resize();
  pieChart?.resize();
}

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  lineChart?.dispose();
  pieChart?.dispose();
});
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader title="数据统计" desc="调用量趋势与状态码分布，便于评估接口质量" />
    <div class="stat-grid" v-if="data">
      <StatCard label="近 7 日调用" :value="data.callTrend.reduce((a, b) => a + b, 0).toLocaleString()" />
      <StatCard label="成功率" :value="data.successRate.toFixed(2) + '%'" />
      <StatCard label="峰值日调用" :value="Math.max(...data.callTrend).toLocaleString()" />
      <StatCard label="监控接口" :value="data.topInterfaces.length" />
    </div>
    <div class="chart-grid">
      <div class="card chart-card">
        <div class="chart-title">调用趋势</div>
        <div ref="lineRef" class="chart" />
      </div>
      <div class="card chart-card">
        <div class="chart-title">状态码分布</div>
        <div ref="pieRef" class="chart" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.chart-card {
  padding: 18px 20px 12px;
}
.chart-title {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.chart {
  height: 300px;
}
</style>
