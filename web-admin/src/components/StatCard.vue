<script setup lang="ts">
import type { Component } from "vue";

defineProps<{
  label: string;
  value: string | number;
  trend?: string;
  up?: boolean;
  icon?: Component;
  tone?: "blue" | "cyan" | "green" | "violet";
}>();
</script>

<template>
  <div class="stat-card card">
    <div v-if="icon" class="icon" :class="tone || 'blue'">
      <el-icon :size="18"><component :is="icon" /></el-icon>
    </div>
    <span class="label">{{ label }}</span>
    <strong class="value">{{ value }}</strong>
    <span v-if="trend" class="trend" :class="{ down: up === false }">
      {{ up === false ? "↓" : "↑" }} {{ trend }}
      <em>较昨日</em>
    </span>
  </div>
</template>

<style scoped>
.stat-card {
  padding: 18px 20px 16px;
  min-height: 132px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  position: relative;
}
.icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  margin-bottom: 4px;
}
.icon.blue {
  background: rgba(37, 99, 235, 0.18);
  color: #60a5fa;
}
.icon.cyan {
  background: rgba(56, 189, 248, 0.16);
  color: #38bdf8;
}
.icon.green {
  background: rgba(34, 197, 94, 0.16);
  color: #4ade80;
}
.icon.violet {
  background: rgba(139, 92, 246, 0.16);
  color: #a78bfa;
}
.label {
  font-size: 12px;
  color: var(--text-muted);
}
.value {
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 0.4px;
  line-height: 1.1;
}
.trend {
  font-size: 12px;
  color: #4ade80;
}
.trend.down {
  color: #f87171;
}
.trend em {
  font-style: normal;
  color: var(--text-muted);
  margin-left: 4px;
}
</style>
