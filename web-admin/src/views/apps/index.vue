<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { Connection, CopyDocument, Key, Plus, View } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  createApp,
  fetchAppDetail,
  fetchApps,
  fetchInterfaces,
  grantInterfaces,
  revokeInterface,
  updateAppStatus,
} from "@/api/admin";
import CanAccess from "@/components/CanAccess.vue";
import PageHeader from "@/components/PageHeader.vue";
import { useAccess } from "@/composables/useAccess";
import type { ApiApp, ApiInterface } from "@/types";

const { Perm, isAdmin } = useAccess();
const loading = ref(false);
const apps = ref<ApiApp[]>([]);
const dialog = ref(false);
const resultDialog = ref(false);
const createdApp = ref<ApiApp | null>(null);
const createdDemo = ref(false);
const creating = ref(false);
const form = reactive({
  appName: "",
  qpsLimit: 10,
  dailyQuota: 1000,
});

// 接口权限管理弹窗
const grantDialog = ref(false);
const grantTarget = ref<ApiApp | null>(null);
const grantLoading = ref(false);
const grantSaving = ref(false);
const allInterfaces = ref<ApiInterface[]>([]);
const checkedIds = ref<number[]>([]);

const summary = computed(() => ({
  totalApps: apps.value.length,
  enabledApps: apps.value.filter((item) => item.status === 1).length,
  totalInvoke: apps.value.reduce((sum, item) => sum + (item.invokeCount || 0), 0),
}));

onMounted(async () => {
  loading.value = true;
  try {
    apps.value = await fetchApps();
  } finally {
    loading.value = false;
  }
});

async function createAppHandler() {
  if (!form.appName.trim()) {
    ElMessage.warning("请输入应用名称");
    return;
  }
  creating.value = true;
  try {
    const { app, demo } = await createApp({
      appName: form.appName.trim(),
      qpsLimit: form.qpsLimit,
      dailyQuota: form.dailyQuota,
    });
    apps.value.unshift(app);
    createdApp.value = app;
    createdDemo.value = demo;
    dialog.value = false;
    resultDialog.value = true;
    form.appName = "";
    form.qpsLimit = 10;
    form.dailyQuota = 1000;
    ElMessage.success(demo ? "应用已创建（离线演示数据）" : "应用已创建");
  } catch (err) {
    ElMessage.error((err as Error).message || "创建失败");
  } finally {
    creating.value = false;
  }
}

async function toggleAppStatus(item: ApiApp) {
  const next = item.status === 1 ? 0 : 1;
  const action = next === 1 ? "启用" : "停用";
  try {
    await ElMessageBox.confirm(`确认${action}应用「${item.appName}」？`, "提示", {
      type: "warning",
    });
  } catch {
    return;
  }
  try {
    await updateAppStatus(item.id, next);
    item.status = next;
    ElMessage.success(`应用已${action}`);
  } catch (err) {
    ElMessage.error((err as Error).message || `${action}失败`);
  }
}

async function copySecret(secret?: string) {
  if (!secret) {
    ElMessage.warning("Secret 仅创建时可见一次，如需重新获取请重新创建应用");
    return;
  }
  await navigator.clipboard.writeText(secret);
  ElMessage.success("AppSecret 已复制");
}

function showSecret(item: ApiApp) {
  if (!item.appSecret) {
    ElMessage.info("Secret 仅创建时可见一次，列表中不展示");
    return;
  }
  ElMessageBox.alert(item.appSecret, "AppSecret", {
    confirmButtonText: "复制",
    callback: () => copySecret(item.appSecret),
  });
}

async function openGrantDialog(item: ApiApp) {
  grantTarget.value = item;
  grantDialog.value = true;
  grantLoading.value = true;
  checkedIds.value = [];
  try {
    const [detail, interfaces] = await Promise.all([fetchAppDetail(item.id), fetchInterfaces()]);
    allInterfaces.value = interfaces;
    checkedIds.value = detail.grantedInterfaces.map((api) => api.id);
  } catch (err) {
    ElMessage.error((err as Error).message || "加载接口列表失败");
  } finally {
    grantLoading.value = false;
  }
}

async function saveGrants() {
  if (!grantTarget.value) return;
  const target = grantTarget.value;
  grantSaving.value = true;
  try {
    const detail = await fetchAppDetail(target.id);
    const currentIds = detail.grantedInterfaces.map((api) => api.id);
    const targetSet = new Set(checkedIds.value);
    const toGrant = checkedIds.value.filter((id) => !currentIds.includes(id));
    const toRevoke = currentIds.filter((id) => !targetSet.has(id));
    if (toGrant.length > 0) {
      await grantInterfaces(target.appId, toGrant);
    }
    for (const id of toRevoke) {
      await revokeInterface(target.appId, id);
    }
    target.interfaceCount = checkedIds.value.length;
    ElMessage.success("接口权限已更新");
    grantDialog.value = false;
  } catch (err) {
    ElMessage.error((err as Error).message || "保存失败");
  } finally {
    grantSaving.value = false;
  }
}
</script>

<template>
  <div class="page" v-loading="loading">
    <PageHeader
      :title="isAdmin ? '应用中心' : '我的应用'"
      :desc="isAdmin ? '管理全部应用凭证、启停与配额' : '创建应用、保存 AppId / Secret，用于调用开放接口'"
    >
      <CanAccess :permission="Perm.APP_CREATE">
        <el-button type="primary" :icon="Plus" @click="dialog = true">创建应用</el-button>
      </CanAccess>
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
            <code class="secret">••••••••••••••••••••••••••</code>
          </div>
          <div class="secret-actions">
            <el-button text type="primary" :icon="View" @click="showSecret(item)">查看 Secret</el-button>
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
          <div class="foot-actions">
            <span class="time">创建于 {{ item.createTime }}</span>
            <CanAccess :permission="Perm.APP_CREATE">
              <el-button size="small" :icon="Connection" @click="openGrantDialog(item)">管理接口</el-button>
            </CanAccess>
            <CanAccess :permission="Perm.APP_MANAGE">
              <el-button size="small" :type="item.status === 1 ? 'warning' : 'success'" @click="toggleAppStatus(item)">
                {{ item.status === 1 ? "停用" : "启用" }}
              </el-button>
            </CanAccess>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialog" title="创建应用" width="460px">
      <el-form label-position="top">
        <el-form-item label="应用名称">
          <el-input v-model="form.appName" placeholder="例如：开放网关演示" maxlength="128" />
        </el-form-item>
        <el-form-item label="QPS 上限">
          <el-input-number v-model="form.qpsLimit" :min="1" :max="100000" />
        </el-form-item>
        <el-form-item label="每日配额">
          <el-input-number v-model="form.dailyQuota" :min="1" :max="100000000" :step="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createAppHandler">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resultDialog" title="创建成功" width="520px">
      <div v-if="createdApp" class="created">
        <div class="created-tip">
          <el-icon><Key /></el-icon>
          <span>请妥善保存以下凭证，离开页面后仅创建时可见一次。</span>
        </div>
        <div class="created-item">
          <span>AppId</span>
          <code>{{ createdApp.appId }}</code>
        </div>
        <div class="created-item">
          <span>AppSecret</span>
          <code>{{ createdApp.appSecret || "（离线演示）" }}</code>
        </div>
        <el-alert v-if="createdDemo" type="warning" :closable="false"
          title="当前为离线演示数据：后端不可用，应用未真正落库。" />
      </div>
      <template #footer>
        <el-button @click="resultDialog = false">关闭</el-button>
        <el-button type="primary" :icon="CopyDocument" @click="copySecret(createdApp?.appSecret)">
          复制 Secret
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="grantDialog" :title="`管理接口权限 · ${grantTarget?.appName || ''}`" width="580px">
      <div v-loading="grantLoading">
        <p class="grant-tip">勾选需要开放的接口，保存后立即生效；仅显示已上线的接口。</p>
        <el-checkbox-group v-model="checkedIds" class="grant-list">
          <el-checkbox v-for="api in allInterfaces" :key="api.id" :value="api.id" class="grant-item">
            <span class="grant-name">{{ api.name }}</span>
            <code class="grant-path">{{ api.method }} {{ api.path }}</code>
          </el-checkbox>
        </el-checkbox-group>
        <el-empty v-if="!grantLoading && allInterfaces.length === 0" description="暂无可开通的接口" :image-size="72" />
      </div>
      <template #footer>
        <el-button @click="grantDialog = false">取消</el-button>
        <el-button type="primary" :loading="grantSaving" @click="saveGrants">保存</el-button>
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
.foot-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
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
.grant-tip {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--text-secondary);
}
.grant-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 320px;
  overflow-y: auto;
  padding-right: 4px;
}
.grant-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.4);
  height: auto;
  white-space: normal;
}
.grant-name {
  font-weight: 600;
  margin-right: 8px;
}
.grant-path {
  font-family: ui-monospace, Consolas, monospace;
  font-size: 12px;
  color: var(--text-secondary);
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
