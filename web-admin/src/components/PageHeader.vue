<script setup lang="ts">
defineProps<{
  title: string;
  desc?: string;
}>();
</script>

<template>
  <div class="page-header">
    <div>
      <h2>{{ title }}</h2>
      <p v-if="desc">{{ desc }}</p>
    </div>
    <div class="actions">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}
</style>
