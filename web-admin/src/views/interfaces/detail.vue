<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowLeft } from "@element-plus/icons-vue";
import { fetchInterfaceById } from "@/api/admin";
import CanAccess from "@/components/CanAccess.vue";
import MethodBadge from "@/components/MethodBadge.vue";
import { useAccess } from "@/composables/useAccess";
import { withLoading } from "@/utils/async";
import type { ApiInterface } from "@/types";

const route = useRoute();
const router = useRouter();
const { Perm, isAdmin } = useAccess();
const loading = ref(true);
const api = ref<ApiInterface | null>(null);

const example = computed(() => api.value?.responseExample || "{}");

onMounted(async () => {
  const id = Number(route.params.id);
  if (!Number.isFinite(id) || id <= 0) {
    ElMessage.error("无效的接口 ID");
    router.replace("/interfaces");
    return;
  }

  const result = await withLoading(loading, () => fetchInterfaceById(id));
  if (!result) {
    ElMessage.error("未找到该接口资产");
    router.replace("/interfaces");
    return;
  }
  api.value = result;
});

function copyPath() {
  if (!api.value) return;
  navigator.clipboard.writeText(api.value.path);
  ElMessage.success("已复制接口路径");
}

function copyJson() {
  navigator.clipboard.writeText(example.value);
  ElMessage.success("已复制响应示例");
}

function toggleStatus() {
  if (!api.value) return;
  api.value.status = api.value.status === 1 ? 0 : 1;
  ElMessage.success(api.value.status === 1 ? "已上线（前端演示）" : "已下线（前端演示）");
}
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="back">
      <el-button :icon="ArrowLeft" text @click="router.push('/interfaces')">
        {{ isAdmin ? "返回资产中心" : "返回接口市场" }}
      </el-button>
    </div>

    <template v-if="api">
      <section class="card hero">
        <div>
          <div class="title-row">
            <h1>{{ api.name }}</h1>
            <el-tag :type="api.status === 1 ? 'success' : 'info'">
              {{ api.status === 1 ? "已上线" : "已下线" }}
            </el-tag>
          </div>
          <p>{{ api.description || "暂无描述" }}</p>
          <div class="path-row">
            <MethodBadge :method="api.method" />
            <code>{{ api.path }}</code>
            <el-button size="small" text type="primary" @click="copyPath">复制</el-button>
          </div>
        </div>
        <div class="hero-meta">
          <CanAccess :permission="Perm.API_MANAGE">
            <el-button
              size="small"
              :type="api.status === 1 ? 'warning' : 'success'"
              @click="toggleStatus"
            >
              {{ api.status === 1 ? "下线接口" : "上线接口" }}
            </el-button>
          </CanAccess>
          <div><span>版本</span><strong>{{ api.version }}</strong></div>
          <div><span>分类</span><strong>{{ api.category || "-" }}</strong></div>
          <div><span>负责人</span><strong>{{ api.owner || "Admin" }}</strong></div>
        </div>
      </section>

      <div class="layout">
        <div class="col">
          <section class="card block">
            <h3>基本信息</h3>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="接口名称">{{ api.name }}</el-descriptions-item>
              <el-descriptions-item label="状态">{{ api.status === 1 ? "已上线" : "已下线" }}</el-descriptions-item>
              <el-descriptions-item label="版本">{{ api.version }}</el-descriptions-item>
              <el-descriptions-item label="分类">{{ api.category || "-" }}</el-descriptions-item>
              <el-descriptions-item label="负责人">{{ api.owner || "Admin" }}</el-descriptions-item>
              <el-descriptions-item label="创建时间">{{ api.createTime || "-" }}</el-descriptions-item>
            </el-descriptions>
          </section>

          <section class="card block">
            <h3>接口地址</h3>
            <p class="hint">经网关统一入口调用，生产环境将域名替换为开放域名。</p>
            <pre class="code">{{ api.method }}  https://api.apihub.local{{ api.path }}</pre>
          </section>

          <section class="card block">
            <h3>请求参数</h3>
            <el-table v-if="api.params && api.params.length" :data="api.params" size="small">
              <el-table-column prop="name" label="参数名" width="120" />
              <el-table-column prop="location" label="位置" width="90" />
              <el-table-column prop="type" label="类型" width="90" />
              <el-table-column label="必填" width="80">
                <template #default="{ row }">{{ row.required ? "是" : "否" }}</template>
              </el-table-column>
              <el-table-column prop="desc" label="说明" />
            </el-table>
            <el-empty v-else description="无需请求参数" :image-size="64" />
          </section>

          <section class="card block">
            <div class="block-head">
              <h3>响应示例</h3>
              <el-button size="small" @click="copyJson">复制 JSON</el-button>
            </div>
            <pre class="code json">{{ example }}</pre>
          </section>
        </div>

        <aside class="side">
          <section class="card block">
            <h3>鉴权方式</h3>
            <p class="auth">{{ api.authType }}</p>
            <ul>
              <li>X-App-Id</li>
              <li>X-Timestamp</li>
              <li>X-Nonce</li>
              <li>X-Sign</li>
            </ul>
          </section>
          <section class="card block">
            <h3>调用统计</h3>
            <div class="stat">
              <span>累计调用</span>
              <strong>{{ (api.callCount || 0).toLocaleString() }}</strong>
            </div>
            <div class="stat">
              <span>近 24h 成功率</span>
              <strong>99.98%</strong>
            </div>
            <div class="stat">
              <span>平均耗时</span>
              <strong>48 ms</strong>
            </div>
          </section>
        </aside>
      </div>
    </template>
  </div>
</template>

<style scoped>
.back {
  margin-bottom: 12px;
}
.hero {
  padding: 22px 24px;
  display: flex;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 16px;
  background:
    linear-gradient(90deg, rgba(37, 99, 235, 0.1), transparent 50%),
    var(--bg-elevated);
}
.title-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.hero h1 {
  margin: 0;
  font-size: 22px;
}
.hero p {
  margin: 8px 0 14px;
  color: var(--text-secondary);
  font-size: 13px;
}
.path-row {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.65);
  border: 1px solid var(--border);
}
code {
  font-family: ui-monospace, Consolas, monospace;
  color: var(--accent);
}
.hero-meta {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 160px;
}
.hero-meta span {
  display: block;
  font-size: 12px;
  color: var(--text-muted);
}
.layout {
  display: grid;
  grid-template-columns: 1.7fr 0.9fr;
  gap: 16px;
}
.block {
  padding: 18px 20px 20px;
  margin-bottom: 16px;
}
.block h3 {
  margin: 0 0 14px;
  font-size: 15px;
}
.block-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.block-head h3 {
  margin: 0;
}
.hint {
  margin: 0 0 10px;
  font-size: 12px;
  color: var(--text-muted);
}
.code {
  margin: 0;
  padding: 14px 16px;
  background: #0b1220;
  border: 1px solid var(--border);
  border-radius: 8px;
  color: #e2e8f0;
  font-size: 13px;
  line-height: 1.6;
  overflow: auto;
  font-family: ui-monospace, Consolas, monospace;
}
.json {
  min-height: 160px;
}
.auth {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--accent);
}
ul {
  margin: 0;
  padding-left: 18px;
  color: var(--text-secondary);
  font-size: 13px;
  line-height: 1.8;
}
.stat {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
  font-size: 13px;
}
.stat:last-child {
  border-bottom: none;
}
.stat span {
  color: var(--text-muted);
}
@media (max-width: 1100px) {
  .layout,
  .hero {
    grid-template-columns: 1fr;
    display: block;
  }
}
</style>
