<script setup lang="ts">
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ArrowRight } from "@element-plus/icons-vue";
import type { BreadcrumbItem } from "@/types";

const route = useRoute();
const router = useRouter();

/** 从路由 meta 或 matched 记录生成面包屑 */
const items = computed<BreadcrumbItem[]>(() => {
  if (route.meta.breadcrumb?.length) {
    return route.meta.breadcrumb;
  }

  const matched = route.matched.filter(
    (record) => record.meta?.title && !record.meta?.hideInBreadcrumb
  );

  return matched.map((record, index) => {
    const isLast = index === matched.length - 1;
    let path: string | undefined;

    if (!isLast && record.name) {
      path = router.resolve({ name: record.name as string, params: route.params }).path;
    }

    return {
      title: record.meta.title as string,
      path,
    };
  });
});

function navigate(path?: string) {
  if (path) router.push(path);
}
</script>

<template>
  <nav v-if="items.length" class="app-breadcrumb" aria-label="面包屑导航">
    <ol class="breadcrumb-list">
      <li v-for="(item, index) in items" :key="index" class="breadcrumb-item">
        <el-icon v-if="index > 0" class="sep"><ArrowRight /></el-icon>
        <button
          v-if="item.path && index < items.length - 1"
          class="crumb-link"
          type="button"
          @click="navigate(item.path)"
        >
          {{ item.title }}
        </button>
        <span v-else class="crumb-current">{{ item.title }}</span>
      </li>
    </ol>
  </nav>
</template>

<style scoped lang="scss">
@use "@/styles/variables" as *;

.app-breadcrumb {
  padding: 12px 24px 0;
  min-height: 36px;

  @include respond-down(sm) {
    padding-inline: 16px;
  }
}

.breadcrumb-list {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.sep {
  font-size: 12px;
  color: var(--text-muted);
}

.crumb-link {
  border: none;
  background: transparent;
  padding: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--accent);
  }
}

.crumb-current {
  font-size: var(--font-size-sm);
  color: var(--text-primary);
  font-weight: $font-weight-medium;
}
</style>
