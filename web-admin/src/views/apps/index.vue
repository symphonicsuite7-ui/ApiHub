<script setup lang="ts">
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { fetchApps } from "@/api/admin";
import PageHeader from "@/components/PageHeader.vue";
import type { ApiApp } from "@/types";

const loading = ref(false);
const apps = ref<ApiApp[]>([]);
const dialog = ref(false);
const form = ref({ appName: "" });

onMounted(async () => {
  loading.value = true;
  apps.value = await fetchApps();
  loading.value = false;
});

function createApp() {
  if (!form.value.appName.trim()) {
    ElMessage.warning("请输入应用名称");
    return;
  }
  ElMessage.success("演示环境：创建流程已展示，待后端应用接口就绪后接入");
  dialog.value = false;
}
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader title="应用中心" desc="管理开放调用方，查看 AppId、配额与运行状态">
      <el-button type="primary" @click="dialog = true">创建应用</el-button>
    </PageHeader>

    <div class="app-grid">
      <div v-for="item in apps" :key="item.id" class="card app-card">
        <div class="app-head">
          <h3>{{ item.appName }}</h3>
          <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">
            {{ item.status === 1 ? "启用" : "停用" }}
          </el-tag>
        </div>
        <p class="appid">{{ item.appId }}</p>
        <div class="meta">
          <span>QPS {{ item.qpsLimit }}</span>
          <span>日配额 {{ item.dailyQuota.toLocaleString() }}</span>
        </div>
        <p class="time">创建于 {{ item.createTime }}</p>
      </div>
    </div>

    <el-dialog v-model="dialog" title="创建应用" width="420px">
      <el-form label-position="top">
        <el-form-item label="应用名称">
          <el-input v-model="form.appName" placeholder="例如：开放网关演示" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="createApp">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.app-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}
.app-card {
  padding: 20px;
}
.app-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.app-head h3 {
  margin: 0;
  font-size: 16px;
}
.appid {
  margin: 12px 0;
  color: var(--accent);
  font-family: ui-monospace, Consolas, monospace;
  font-size: 13px;
}
.meta {
  display: flex;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 13px;
}
.time {
  margin: 16px 0 0;
  color: var(--text-muted);
  font-size: 12px;
}
@media (max-width: 1100px) {
  .app-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
