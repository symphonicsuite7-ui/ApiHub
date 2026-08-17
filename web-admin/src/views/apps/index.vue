<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { CopyDocument, Key, Plus, View } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { fetchApps } from "@/api/admin";
import PageHeader from "@/components/PageHeader.vue";
import type { ApiApp } from "@/types";

const loading = ref(false);
const apps = ref<ApiApp[]>([]);
const dialog = ref(false);
const resultDialog = ref(false);
const visibleSecrets = ref<number[]>([]);
const form = reactive({
  appName: "",
  description: "",
});
const createdApp = ref<ApiApp | null>(null);

const summary = computed(() => ({
  totalApps: apps.value.length,
  enabledApps: apps.value.filter((item) => item.status === 1).length,
  totalInvoke: apps.value.reduce((sum, item) => sum + (item.invokeCount || 0), 0),
}));

onMounted(async () => {
  loading.value = true;
  apps.value = await fetchApps();
  loading.value = false;
});

function createApp() {
  if (!form.appName.trim()) {
    ElMessage.warning("请输入应用名称");
    return;
  }
  const stamp = Date.now().toString(36).slice(-8);
  const newApp: ApiApp = {
    id: Date.now(),
    appId: `app_${stamp}`,
    appName: form.appName.trim(),
    description: form.description.trim() || "开发者中心新创建的业务应用。",
    appSecret: `sk_live_${Date.now().toString(36)}${Math.random().toString(36).slice(2, 8)}`,
    status: 1,
    qpsLimit: 10,
    dailyQuota: 1000,
    interfaceCount: 0,
    invokeCount: 0,
    owner: "Current User",
    createTime: new Date().toLocaleString("zh-CN", { hour12: false }),
  };
  apps.value.unshift(newApp);
  createdApp.value = newApp;
  dialog.value = false;
  resultDialog.value = true;
  form.appName = "";
  form.description = "";
  ElMessage.success("应用已创建");
}

function maskedSecret(secret?: string) {
  if (!secret) return "********";
  return `${secret.slice(0, 4)}****************`;
}

function isVisible(id: number) {
  return visibleSecrets.value.includes(id);
}

function toggleSecret(id: number) {
  if (isVisible(id)) {
    visibleSecrets.value = visibleSecrets.value.filter((item) => item !== id);
  } else {
    visibleSecrets.value = [...visibleSecrets.value, id];
  }
}

async function copySecret(secret?: string) {
  if (!secret) {
    ElMessage.warning("暂无 Secret");
    return;
  }
  await navigator.clipboard.writeText(secret);
  ElMessage.success("AppSecret 已复制");
}
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader title="我的应用" desc="模拟企业开发者中心，管理 AppId / Secret 与授权能力">
      <el-button type="primary" :icon="Plus" @click="dialog = true">创建应用</el-button>
    </PageHeader>

    <div class="summary-row">
      <div class="summary card">
        <span>应用总数</span>
        <strong>{{ summary.totalApps }}</strong>
      </div>
      <div class="summary card">
        <span>已启用应用</span>
        <strong>{{ summary.enabledApps }}</strong>
      </div>
      <div class="summary card">
        <span>累计调用次数</span>
        <strong>{{ summary.totalInvoke.toLocaleString() }}</strong>
      </div>
    </div>

    <div class="app-grid">
      <div v-for="item in apps" :key="item.id" class="card app-card">
        <div class="app-head">
          <div>
            <h3>{{ item.appName }}</h3>
            <p class="desc">{{ item.description || "暂无描述" }}</p>
          </div>
          <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">
            {{ item.status === 1 ? "已启用" : "已停用" }}
          </el-tag>
        </div>

        <div class="credential-block">
          <div class="row">
            <span class="label">AppId</span>
            <code class="appid">{{ item.appId }}</code>
          </div>
          <div class="row">
            <span class="label">Secret</span>
            <code class="secret">{{ isVisible(item.id) ? item.appSecret : maskedSecret(item.appSecret) }}</code>
          </div>
          <div class="secret-actions">
            <el-button text type="primary" :icon="View" @click="toggleSecret(item.id)">
              {{ isVisible(item.id) ? "隐藏 Secret" : "显示 Secret" }}
            </el-button>
            <el-button text type="primary" :icon="CopyDocument" @click="copySecret(item.appSecret)">
              复制 Secret
            </el-button>
          </div>
        </div>

        <div class="meta-grid">
          <div class="metric">
            <span>授权接口数量</span>
            <strong>{{ item.interfaceCount || 0 }}</strong>
          </div>
          <div class="metric">
            <span>调用次数</span>
            <strong>{{ (item.invokeCount || 0).toLocaleString() }}</strong>
          </div>
          <div class="metric">
            <span>QPS</span>
            <strong>{{ item.qpsLimit }}</strong>
          </div>
          <div class="metric">
            <span>日配额</span>
            <strong>{{ item.dailyQuota.toLocaleString() }}</strong>
          </div>
        </div>

        <div class="foot">
          <span class="owner">负责人 {{ item.owner || "Admin" }}</span>
          <span class="time">创建于 {{ item.createTime }}</span>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialog" title="创建应用" width="460px">
      <el-form label-position="top">
        <el-form-item label="应用名称">
          <el-input v-model="form.appName" placeholder="例如：开放网关演示" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="简要说明应用用途，例如：用于运营系统接入 Weather API 与 SMS API"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" @click="createApp">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialog" title="创建成功" width="520px">
      <div v-if="createdApp" class="created">
        <div class="created-tip">
          <el-icon><Key /></el-icon>
          <span>请妥善保存以下凭证，离开页面后仅支持重新生成 Secret。</span>
        </div>
        <div class="created-item">
          <span>AppId</span>
          <code>{{ createdApp.appId }}</code>
        </div>
        <div class="created-item">
          <span>AppSecret</span>
          <code>{{ createdApp.appSecret }}</code>
        </div>
      </div>
      <template #footer>
        <el-button @click="resultDialog = false">关闭</el-button>
        <el-button type="primary" :icon="CopyDocument" @click="copySecret(createdApp?.appSecret)">
          复制 Secret
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.summary-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}
.summary {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.summary span {
  font-size: 12px;
  color: var(--text-muted);
}
.summary strong {
  font-size: 24px;
  font-weight: 700;
}
.app-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}
.app-card {
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.app-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.app-head h3 {
  margin: 0;
  font-size: 16px;
}
.desc {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.5;
}
.credential-block {
  padding: 14px 14px 12px;
  border: 1px solid var(--border);
  border-radius: 10px;
  background: rgba(15, 23, 42, 0.55);
}
.row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.row:last-of-type {
  margin-bottom: 8px;
}
.label {
  width: 52px;
  flex-shrink: 0;
  font-size: 12px;
  color: var(--text-muted);
}
.appid,
.secret {
  color: var(--accent);
  font-family: ui-monospace, Consolas, monospace;
  font-size: 13px;
  word-break: break-all;
}
.secret-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}
.metric {
  padding: 12px 10px;
  border-radius: 10px;
  background: rgba(30, 41, 59, 0.48);
  border: 1px solid rgba(51, 65, 85, 0.5);
}
.metric span {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.metric strong {
  font-size: 18px;
}
.foot {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}
.owner,
.time {
  font-size: 12px;
}
.owner {
  color: var(--text-secondary);
}
.created {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.created-tip {
  display: flex;
  gap: 10px;
  align-items: flex-start;
  padding: 12px 14px;
  border-radius: 10px;
  background: rgba(37, 99, 235, 0.12);
  color: #bfdbfe;
  font-size: 13px;
}
.created-item {
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--border);
  background: rgba(15, 23, 42, 0.55);
}
.created-item span {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
}
.created-item code {
  color: var(--accent);
  font-family: ui-monospace, Consolas, monospace;
  word-break: break-all;
}
@media (max-width: 1200px) {
  .meta-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (max-width: 1100px) {
  .summary-row,
  .app-grid {
    grid-template-columns: 1fr;
  }
}
</style>
