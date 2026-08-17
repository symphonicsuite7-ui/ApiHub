<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { Connection, Cpu, Grid, Histogram, Monitor } from "@element-plus/icons-vue";
import { fetchLogs, fetchOverview } from "@/api/admin";
import { useUserStore } from "@/stores/user";
import StatCard from "@/components/StatCard.vue";
import type { InvokeLog, OverviewStat } from "@/types";

const userStore = useUserStore();
const loading = ref(true);
const data = ref<OverviewStat | null>(null);
const logs = ref<InvokeLog[]>([]);
const nowText = ref("");
const trendRef = ref<HTMLDivElement | null>(null);
let trendChart: echarts.ECharts | null = null;
let clockTimer: number | null = null;

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) return "上午好";
  if (hour < 18) return "下午好";
  return "晚上好";
});

const services = [
  { name: "Gateway", port: "8080", status: "正常" },
  { name: "Auth Service", port: "8081", status: "正常" },
  { name: "Admin Service", port: "8082", status: "正常" },
  { name: "Invoke Service", port: "8083", status: "正常" },
];

const rankMax = computed(() => {
  const list = data.value?.topInterfaces || [];
  return Math.max(...list.map((i) => i.value), 1);
});

function tickClock() {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  nowText.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

function renderTrend(stat: OverviewStat) {
  if (!trendRef.value) return;
  trendChart = echarts.init(trendRef.value);
  trendChart.setOption({
    backgroundColor: "transparent",
    tooltip: {
      trigger: "axis",
      backgroundColor: "#111827",
      borderColor: "#334155",
      textStyle: { color: "#f8fafc" },
    },
    grid: { left: 48, right: 16, top: 28, bottom: 32 },
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
        name: "调用次数",
        type: "line",
        smooth: true,
        data: stat.callTrend,
        symbol: "circle",
        symbolSize: 7,
        itemStyle: { color: "#38bdf8" },
        lineStyle: { color: "#2563eb", width: 2.5 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: "rgba(37, 99, 235, 0.35)" },
            { offset: 1, color: "rgba(37, 99, 235, 0.02)" },
          ]),
        },
      },
    ],
  });
}

function statusType(code: number) {
  if (code >= 200 && code < 300) return "success";
  if (code === 429) return "warning";
  return "danger";
}

function statusText(code: number) {
  if (code >= 200 && code < 300) return "成功";
  if (code === 429) return "限流";
  return "失败";
}

function resize() {
  trendChart?.resize();
}

onMounted(async () => {
  tickClock();
  clockTimer = window.setInterval(tickClock, 1000);
  const [overview, logList] = await Promise.all([fetchOverview(), fetchLogs()]);
  data.value = overview;
  logs.value = logList;
  loading.value = false;
  requestAnimationFrame(() => renderTrend(overview));
  window.addEventListener("resize", resize);
});

onBeforeUnmount(() => {
  if (clockTimer) window.clearInterval(clockTimer);
  window.removeEventListener("resize", resize);
  trendChart?.dispose();
});
</script>

<template>
  <div class="page dash" v-loading="loading">
    <!-- 欢迎区 -->
    <section class="hero card">
      <div>
        <h1>ApiHub 控制台</h1>
        <p>企业微服务接口管理平台</p>
        <div class="hello">
          {{ greeting }}，{{ userStore.displayName }}
        </div>
      </div>
      <div class="sys-status">
        <span class="sys-label">今日系统状态</span>
        <div class="sys-row">
          <span class="pulse" />
          <strong>运行正常</strong>
        </div>
        <span class="sys-time">{{ nowText }}</span>
      </div>
    </section>

    <!-- 核心指标 -->
    <div class="stat-grid" v-if="data">
      <StatCard label="API 数量" :value="data.apiCount" :trend="data.apiTrend" :icon="Grid" tone="blue" />
      <StatCard
        label="今日调用次数"
        :value="data.todayCalls.toLocaleString()"
        :trend="data.callTrendPct"
        :icon="Histogram"
        tone="cyan"
      />
      <StatCard
        label="接口成功率"
        :value="data.successRate.toFixed(2) + '%'"
        :trend="data.successTrend"
        :icon="Monitor"
        tone="green"
      />
      <StatCard label="活跃应用数量" :value="data.activeApps" :trend="data.appTrend" :icon="Connection" tone="violet" />
    </div>

    <!-- 数据分析 -->
    <div class="chart-grid">
      <section class="card panel">
        <div class="panel-head">
          <div>
            <h3>API 调用趋势</h3>
            <p>近 7 天调用次数</p>
          </div>
        </div>
        <div ref="trendRef" class="trend-chart" />
      </section>

      <section class="card panel">
        <div class="panel-head">
          <div>
            <h3>热门接口排行</h3>
            <p>今日调用量 Top</p>
          </div>
        </div>
        <ul class="rank-list" v-if="data">
          <li v-for="(item, index) in data.topInterfaces" :key="item.name">
            <span class="rank" :class="'r' + (index + 1)">{{ index + 1 }}</span>
            <div class="rank-body">
              <div class="rank-meta">
                <strong>{{ item.name }}</strong>
                <span>{{ item.value.toLocaleString() }}</span>
              </div>
              <div class="bar">
                <i :style="{ width: (item.value / rankMax) * 100 + '%' }" />
              </div>
            </div>
          </li>
        </ul>
      </section>
    </div>

    <!-- 实时日志 + 服务健康 -->
    <div class="bottom-grid">
      <section class="card panel">
        <div class="panel-head">
          <div>
            <h3>实时调用日志</h3>
            <p>最近开放接口调用</p>
          </div>
        </div>
        <el-table :data="logs" size="small">
          <el-table-column label="TraceId" min-width="150">
            <template #default="{ row }">
              <span class="mono">{{ row.traceId }}</span>
            </template>
          </el-table-column>
          <el-table-column label="接口名称" min-width="130">
            <template #default="{ row }">{{ row.name || row.path }}</template>
          </el-table-column>
          <el-table-column prop="createTime" label="调用时间" min-width="160" />
          <el-table-column label="耗时" width="90">
            <template #default="{ row }">{{ row.costMs }} ms</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="statusType(row.statusCode)">{{ statusText(row.statusCode) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="card panel">
        <div class="panel-head">
          <div>
            <h3>服务健康状态</h3>
            <p>微服务集群运行情况</p>
          </div>
        </div>
        <div class="svc-list">
          <div v-for="item in services" :key="item.name" class="svc">
            <div class="svc-icon">
              <el-icon><Cpu /></el-icon>
            </div>
            <div class="svc-meta">
              <strong>{{ item.name }}</strong>
              <span>:{{ item.port }}</span>
            </div>
            <el-tag size="small" type="success">{{ item.status }}</el-tag>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.dash {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  padding: 24px 28px;
  background:
    linear-gradient(90deg, rgba(37, 99, 235, 0.12), transparent 55%),
    var(--bg-elevated);
}

.hero h1 {
  margin: 0 0 6px;
  font-size: 24px;
  font-weight: 700;
}

.hero p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.hello {
  margin-top: 14px;
  font-size: 13px;
  color: var(--accent);
}

.sys-status {
  text-align: right;
}

.sys-label {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 8px;
}

.sys-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.55);
  animation: pulse 1.8s infinite;
}

.sys-time {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}

.panel {
  padding: 18px 20px 16px;
}

.panel-head {
  margin-bottom: 12px;
}

.panel-head h3 {
  margin: 0 0 4px;
  font-size: 15px;
}

.panel-head p {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

.trend-chart {
  height: 280px;
}

.rank-list {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.rank-list li {
  display: flex;
  gap: 12px;
  align-items: center;
}

.rank {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  background: var(--bg-subtle);
  color: var(--text-secondary);
}

.rank.r1 {
  background: rgba(37, 99, 235, 0.25);
  color: #93c5fd;
}
.rank.r2 {
  background: rgba(56, 189, 248, 0.18);
  color: #38bdf8;
}
.rank.r3 {
  background: rgba(148, 163, 184, 0.18);
  color: #cbd5e1;
}

.rank-body {
  flex: 1;
}

.rank-meta {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.rank-meta span {
  color: var(--text-secondary);
  font-variant-numeric: tabular-nums;
}

.bar {
  height: 6px;
  border-radius: 99px;
  background: #1e293b;
  overflow: hidden;
}

.bar i {
  display: block;
  height: 100%;
  border-radius: 99px;
  background: linear-gradient(90deg, #2563eb, #38bdf8);
}

.bottom-grid {
  display: grid;
  grid-template-columns: 1.7fr 1fr;
  gap: 16px;
}

.mono {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 12px;
  color: var(--accent);
}

.svc-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.svc {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.45);
}

.svc-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: grid;
  place-items: center;
  background: rgba(37, 99, 235, 0.16);
  color: #60a5fa;
}

.svc-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.svc-meta strong {
  font-size: 13px;
}

.svc-meta span {
  font-size: 12px;
  color: var(--text-muted);
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
  .bottom-grid {
    grid-template-columns: 1fr;
  }
  .hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  .sys-status {
    text-align: left;
  }
  .sys-row {
    justify-content: flex-start;
  }
}
</style>
