<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { fetchLogs } from "@/api/admin";
import PageHeader from "@/components/PageHeader.vue";
import type { InvokeLog } from "@/types";

const loading = ref(false);
const logs = ref<InvokeLog[]>([]);
const keyword = ref("");

const filtered = computed(() =>
  logs.value.filter(
    (item) =>
      !keyword.value ||
      item.traceId.includes(keyword.value) ||
      item.appId.includes(keyword.value) ||
      item.path.includes(keyword.value)
  )
);

onMounted(async () => {
  loading.value = true;
  logs.value = await fetchLogs();
  loading.value = false;
});

function copyTrace(id: string) {
  navigator.clipboard.writeText(id);
  ElMessage.success("已复制 TraceId");
}

function statusType(code: number) {
  if (code >= 200 && code < 300) return "success";
  if (code === 429) return "warning";
  return "danger";
}
</script>

<template>
  <div class="page">
    <PageHeader title="调用日志" desc="按 TraceId、AppId 检索开放接口调用记录" />
    <div class="card toolbar">
      <el-input v-model="keyword" placeholder="TraceId / AppId / 路径" clearable style="width: 320px" />
    </div>
    <div class="card table-card">
      <el-table :data="filtered" v-loading="loading">
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="TraceId" min-width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="copyTrace(row.traceId)">{{ row.traceId }}</el-button>
          </template>
        </el-table-column>
        <el-table-column prop="appId" label="AppId" min-width="140" />
        <el-table-column prop="path" label="路径" min-width="200" />
        <el-table-column prop="method" label="方法" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.statusCode)">{{ row.statusCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costMs" label="耗时(ms)" width="100" />
        <el-table-column prop="ip" label="IP" width="140" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.toolbar {
  padding: 16px 20px;
  margin-bottom: 16px;
}
.table-card {
  padding: 8px 8px 16px;
}
</style>
