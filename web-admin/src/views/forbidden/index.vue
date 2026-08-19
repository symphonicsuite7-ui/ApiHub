<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Lock } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();

const fromPath = computed(() => (route.query.from as string) || "");
</script>

<template>
  <div class="page forbidden">
    <div class="card panel">
      <el-icon class="icon" :size="48"><Lock /></el-icon>
      <h2>403 · 无权访问</h2>
      <p>当前账号没有访问该页面的权限，请联系管理员开通。</p>
      <p v-if="fromPath" class="from">请求路径：<code>{{ fromPath }}</code></p>
      <p class="from">管理员页面不会出现在侧栏，直接输入地址也会被拦截。</p>
      <div class="actions">
        <el-button type="primary" @click="router.push('/dashboard')">返回控制台</el-button>
        <el-button @click="router.back()">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.forbidden {
  display: grid;
  place-items: center;
  min-height: calc(100vh - var(--header-h) - 80px);
}

.panel {
  max-width: 480px;
  width: 100%;
  padding: 40px 32px;
  text-align: center;
}

.icon {
  color: var(--warning);
  margin-bottom: 16px;
}

h2 {
  margin: 0 0 10px;
  font-size: var(--font-size-xl);
}

p {
  margin: 0 0 8px;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.from code {
  color: var(--accent);
  font-family: var(--font-family-mono);
}

.actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}
</style>
