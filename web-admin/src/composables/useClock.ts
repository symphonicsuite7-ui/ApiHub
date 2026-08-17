import { onBeforeUnmount, onMounted, ref } from "vue";

/** 实时时钟（企业监控大屏通用） */
export function useClock(intervalMs = 1000) {
  const nowText = ref("");

  function tick() {
    const d = new Date();
    const pad = (n: number) => String(n).padStart(2, "0");
    nowText.value = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
  }

  let timer: number | null = null;

  onMounted(() => {
    tick();
    timer = window.setInterval(tick, intervalMs);
  });

  onBeforeUnmount(() => {
    if (timer) window.clearInterval(timer);
  });

  return { nowText };
}
