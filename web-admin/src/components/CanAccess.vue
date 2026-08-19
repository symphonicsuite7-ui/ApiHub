<script setup lang="ts">
import { computed } from "vue";
import { useAccess } from "@/composables/useAccess";

const props = defineProps<{
  /** 需要的权限，满足其一即可 */
  permission?: string | string[];
  /** 需要的角色，满足其一即可 */
  role?: string | string[];
}>();

const { canAny, canRole } = useAccess();

const visible = computed(() => {
  if (props.permission) {
    const list = Array.isArray(props.permission) ? props.permission : [props.permission];
    if (!canAny(list)) return false;
  }
  if (props.role) {
    const list = Array.isArray(props.role) ? props.role : [props.role];
    if (!list.some((item) => canRole(item))) return false;
  }
  return true;
});
</script>

<template>
  <span v-if="visible" class="can-access">
    <slot />
  </span>
</template>

<style scoped>
.can-access {
  display: contents;
}
</style>
