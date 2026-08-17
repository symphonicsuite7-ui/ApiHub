<script setup lang="ts">
import type { HttpMethod } from "@/types";

defineProps<{
  method: HttpMethod | string;
}>();
</script>

<template>
  <span class="method-badge" :class="'method-' + method.toLowerCase()">{{ method }}</span>
</template>

<style scoped lang="scss">
.method-badge {
  font-size: var(--font-size-xs);
  font-weight: 700;
  padding: 2px 8px;
  border-radius: var(--radius-xs);
  letter-spacing: 0.4px;
  flex-shrink: 0;
}

.method-get {
  background: rgba(37, 99, 235, 0.2);
  color: #93c5fd;
}
.method-post {
  background: rgba(34, 197, 94, 0.18);
  color: #4ade80;
}
.method-put {
  background: rgba(245, 158, 11, 0.18);
  color: #fbbf24;
}
.method-delete {
  background: rgba(239, 68, 68, 0.18);
  color: #f87171;
}
</style>
