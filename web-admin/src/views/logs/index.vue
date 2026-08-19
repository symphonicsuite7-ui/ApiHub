<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { CopyDocument, Search } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { fetchLogs } from "@/api/admin";
import CanAccess from "@/components/CanAccess.vue";
import PageHeader from "@/components/PageHeader.vue";
import { useAccess } from "@/composables/useAccess";
import { withLoading } from "@/utils/async";
import { logBarClass, spanStatusColor, statusLabel, statusTagType } from "@/utils/httpStatus";
import type { InvokeLog, TraceSpan } from "@/types";

const { Perm, isAdmin } = useAccess();
const loading = ref(false);
const logs = ref<InvokeLog[]>([]);
const drawer = ref(false);
const current = ref<InvokeLog | null>(null);

const filters = ref({
  traceId: "",
  appId: "",
  apiName: "",
  timeRange: [] as string[],
});

const filtered = computed(() =>
  logs.value.filter((item) => {
    const hitTrace = !filters.value.traceId || item.traceId.includes(filters.value.traceId);
    const hitApp = !filters.value.appId || item.appId.includes(filters.value.appId);
    const hitApi =
      !filters.value.apiName ||
      (item.name || "").toLowerCase().includes(filters.value.apiName.toLowerCase()) ||
      item.path.toLowerCase().includes(filters.value.apiName.toLowerCase());
    let hitTime = true;
    if (filters.value.timeRange?.length === 2) {
      const t = new Date(item.createTime.replace(/-/g, "/")).getTime();
      const start = new Date(filters.value.timeRange[0]).getTime();
      const end = new Date(filters.value.timeRange[1]).getTime() + 86400000;
      hitTime = t >= start && t <= end;
    }
    return hitTrace && hitApp && hitApi && hitTime;
  })
);

onMounted(async () => {
  const result = await withLoading(loading, () => fetchLogs());
  if (result) logs.value = result;
});

function openDetail(item: InvokeLog) {
  current.value = item;
  drawer.value = true;
}

async function copyTrace(id: string) {
  await navigator.clipboard.writeText(id);
  ElMessage.success("TraceId 已复制");
}

function jsonPretty(data: unknown) {
  return JSON.stringify(data, null, 2);
}

function resetFilters() {
  filters.value = { traceId: "", appId: "", apiName: "", timeRange: [] };
}

function exportLogs() {
  ElMessage.success("演示环境：调用日志导出任务已创建，待后端接入后下载文件");
}

function spanColor(status: TraceSpan["status"]) {
  return spanStatusColor(status);
}
</script>

<template>
  <div class="page trace-page" v-loading="loading">
    <PageHeader
      :title="isAdmin ? '调用链追踪' : '我的调用记录'"
      :desc="isAdmin ? '全平台 TraceId 检索与耗时分析' : '仅展示当前账号应用产生的调用记录'"
    >
      <CanAccess :permission="Perm.LOG_VIEW_ALL">
        <el-button type="primary" @click="exportLogs">导出日志</el-button>
      </CanAccess>
    </PageHeader>

    <section class="card search-panel">
      <div class="search-title">
        <el-icon><Search /></el-icon>
        <span>链路检索</span>
      </div>
      <div class="search-grid">
        <el-input v-model="filters.traceId" placeholder="TraceId" clearable />
        <el-input v-model="filters.appId" placeholder="AppId" clearable />
        <el-input v-model="filters.apiName" placeholder="接口名称" clearable />
        <el-date-picker
          v-model="filters.timeRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </div>
      <div class="search-actions">
        <el-button @click="resetFilters">重置</el-button>
        <span class="result-count">共 {{ filtered.length }} 条链路</span>
      </div>
    </section>

    <section class="log-list">
      <article
        v-for="item in filtered"
        :key="item.id"
        class="card log-row"
        :class="logBarClass(item.statusCode)"
        @click="openDetail(item)"
      >
        <div class="row-main">
          <div class="trace-line">
            <button class="trace-id" type="button" @click.stop="copyTrace(item.traceId)">
              {{ item.traceId }}
              <el-icon><CopyDocument /></el-icon>
            </button>
            <span class="time">{{ item.createTime }}</span>
          </div>
          <div class="row-meta">
            <div class="cell">
              <span class="label">接口</span>
              <strong>{{ item.name || item.path }}</strong>
              <el-tag size="small" class="method">{{ item.method }}</el-tag>
            </div>
            <div class="cell">
              <span class="label">调用方</span>
              <strong>{{ item.callerName }}</strong>
              <code>{{ item.appId }}</code>
            </div>
            <div class="cell">
              <span class="label">耗时</span>
              <strong class="cost">{{ item.costMs }} ms</strong>
              <div class="mini-bar">
                <i :style="{ width: Math.min(item.costMs / 2.5, 100) + '%' }" />
              </div>
            </div>
            <div class="cell status-cell">
              <span class="label">状态</span>
              <el-tag size="small" :type="statusTagType(item.statusCode)">
                {{ statusLabel(item.statusCode) }} · {{ item.statusCode }}
              </el-tag>
            </div>
          </div>
        </div>
        <div class="row-hint">点击查看完整调用链</div>
      </article>
      <el-empty v-if="!loading && filtered.length === 0" description="未找到匹配的调用链" />
    </section>

    <el-drawer v-model="drawer" title="调用链详情" size="560px" destroy-on-close>
      <template v-if="current">
        <div class="drawer-head">
          <code class="trace-big">{{ current.traceId }}</code>
          <el-tag :type="statusTagType(current.statusCode)">{{ statusLabel(current.statusCode) }}</el-tag>
        </div>

        <section class="drawer-block">
          <h4>调用链</h4>
          <div class="chain">
            <div v-for="(span, index) in current.spans" :key="span.name" class="chain-node">
              <div class="node-card">
                <div class="node-top">
                  <span class="dot" :style="{ background: spanColor(span.status) }" />
                  <strong>{{ span.name }}</strong>
                  <span class="span-ms">{{ span.costMs }} ms</span>
                </div>
                <span class="node-svc">{{ span.service }}</span>
                <div class="span-bar">
                  <i
                    :style="{
                      width: (span.costMs / current.costMs) * 100 + '%',
                      background: spanColor(span.status),
                    }"
                  />
                </div>
              </div>
              <div v-if="index < (current.spans?.length || 0) - 1" class="chain-arrow">↓</div>
            </div>
          </div>
        </section>

        <section class="drawer-block">
          <h4>请求参数</h4>
          <pre class="code-block">{{ jsonPretty(current.requestParams) }}</pre>
        </section>

        <section class="drawer-block">
          <h4>响应结果</h4>
          <pre class="code-block">{{ jsonPretty(current.responseBody) }}</pre>
        </section>

        <section class="drawer-block">
          <h4>耗时分析</h4>
          <div class="duration-list">
            <div v-for="slice in current.durationBreakdown" :key="slice.label" class="duration-item">
              <div class="duration-meta">
                <span>{{ slice.label }}</span>
                <strong>{{ slice.ms }} ms · {{ slice.percent }}%</strong>
              </div>
              <div class="duration-bar">
                <i :style="{ width: slice.percent + '%' }" />
              </div>
            </div>
          </div>
          <div class="total-cost">总耗时 <strong>{{ current.costMs }} ms</strong></div>
        </section>
      </template>
    </el-drawer>
  </div>
</template>

<style scoped>
.trace-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-panel {
  padding: 18px 20px;
}

.search-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 600;
  color: var(--accent);
}

.search-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.search-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 14px;
}

.result-count {
  font-size: 12px;
  color: var(--text-muted);
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-row {
  padding: 0;
  overflow: hidden;
  cursor: pointer;
  border-left: 4px solid transparent;
  transition: border-color 0.15s, transform 0.15s;
}

.log-row:hover {
  transform: translateY(-1px);
}

.log-row.bar-ok {
  border-left-color: #22c55e;
}
.log-row.bar-warn {
  border-left-color: #f59e0b;
}
.log-row.bar-error {
  border-left-color: #ef4444;
}

.row-main {
  padding: 16px 18px 10px;
}

.trace-line {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.trace-id {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: rgba(56, 189, 248, 0.12);
  color: var(--accent);
  font-family: ui-monospace, Consolas, monospace;
  font-size: 13px;
  padding: 6px 10px;
  border-radius: 6px;
  cursor: pointer;
}

.time {
  font-size: 12px;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}

.row-meta {
  display: grid;
  grid-template-columns: 1.2fr 1.2fr 0.8fr 0.7fr;
  gap: 16px;
}

.cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.label {
  font-size: 11px;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.4px;
}

.cell strong {
  font-size: 14px;
}

.cell code {
  font-size: 11px;
  color: var(--text-secondary);
  font-family: ui-monospace, Consolas, monospace;
}

.method {
  width: fit-content;
  margin-top: 2px;
}

.cost {
  color: #38bdf8;
  font-variant-numeric: tabular-nums;
}

.mini-bar {
  height: 4px;
  background: #1e293b;
  border-radius: 99px;
  overflow: hidden;
  margin-top: 4px;
}

.mini-bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #38bdf8);
  border-radius: 99px;
}

.status-cell {
  align-items: flex-start;
}

.row-hint {
  padding: 8px 18px;
  font-size: 11px;
  color: var(--text-muted);
  background: rgba(15, 23, 42, 0.45);
  border-top: 1px solid var(--border);
}

.drawer-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.trace-big {
  font-family: ui-monospace, Consolas, monospace;
  color: var(--accent);
  font-size: 14px;
  word-break: break-all;
}

.drawer-block {
  margin-bottom: 22px;
}

.drawer-block h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: var(--text-secondary);
}

.chain {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.chain-node {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.node-card {
  width: 100%;
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: rgba(15, 23, 42, 0.55);
}

.node-top {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.span-ms {
  margin-left: auto;
  font-size: 12px;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.node-svc {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 10px;
}

.span-bar {
  height: 6px;
  background: #1e293b;
  border-radius: 99px;
  overflow: hidden;
}

.span-bar i {
  display: block;
  height: 100%;
  border-radius: 99px;
  min-width: 4px;
}

.chain-arrow {
  color: var(--text-muted);
  font-size: 18px;
  line-height: 1;
  padding: 6px 0;
}

.code-block {
  margin: 0;
  padding: 14px 16px;
  background: #0b1220;
  border: 1px solid var(--border);
  border-radius: 8px;
  color: #e2e8f0;
  font-size: 12px;
  line-height: 1.6;
  overflow: auto;
  font-family: ui-monospace, Consolas, monospace;
}

.duration-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.duration-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  margin-bottom: 6px;
}

.duration-meta strong {
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.duration-bar {
  height: 8px;
  background: #1e293b;
  border-radius: 99px;
  overflow: hidden;
}

.duration-bar i {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #38bdf8);
  border-radius: 99px;
}

.total-cost {
  margin-top: 14px;
  text-align: right;
  font-size: 13px;
  color: var(--text-secondary);
}

.total-cost strong {
  color: #fff;
  font-size: 16px;
}

@media (max-width: 1100px) {
  .search-grid,
  .row-meta {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 720px) {
  .search-grid,
  .row-meta {
    grid-template-columns: 1fr;
  }
}
</style>
