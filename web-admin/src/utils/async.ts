import type { Ref } from "vue";
import { ElMessage } from "element-plus";

/** 带 loading 与统一错误提示的异步执行 */
export async function withLoading<T>(
  loading: Ref<boolean>,
  task: () => Promise<T>,
  errorMessage = "加载失败"
): Promise<T | undefined> {
  loading.value = true;
  try {
    return await task();
  } catch (error) {
    const msg = error instanceof Error ? error.message : errorMessage;
    ElMessage.error(msg);
    return undefined;
  } finally {
    loading.value = false;
  }
}
