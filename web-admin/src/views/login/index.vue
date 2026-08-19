<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { register } from "@/api/auth";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const mode = ref<"login" | "register">("login");
const loading = ref(false);

const loginForm = reactive({ username: "", password: "" });
const registerForm = reactive({ username: "", nickname: "", password: "" });

async function onLogin() {
  loading.value = true;
  try {
    await userStore.login(loginForm.username, loginForm.password);
    ElMessage.success("登录成功");
    await router.replace("/dashboard");
  } catch (err) {
    ElMessage.error((err as Error).message || "登录失败");
  } finally {
    loading.value = false;
  }
}

async function onRegister() {
  loading.value = true;
  try {
    await register(registerForm);
    ElMessage.success("注册成功，请登录");
    mode.value = "login";
    loginForm.username = registerForm.username;
  } catch (err) {
    ElMessage.error((err as Error).message || "注册失败");
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-page">
    <div class="glow" />
    <aside class="brand-panel">
      <div class="brand">
        <span class="logo">AH</span>
        <div>
          <h1>ApiHub</h1>
          <p>企业微服务集成化接口平台</p>
        </div>
      </div>
      <ul>
        <li>
          <strong>统一网关</strong>
          <span>鉴权、限流、路由与 TraceId 全链路追踪</span>
        </li>
        <li>
          <strong>开放调用</strong>
          <span>应用密钥、接口资产与调用审计一体管理</span>
        </li>
        <li>
          <strong>可观测</strong>
          <span>调用量、成功率与日志检索，支撑企业集成</span>
        </li>
      </ul>
    </aside>

    <section class="form-panel">
      <div class="form-card card">
        <h2>{{ mode === "login" ? "登录控制台" : "创建账号" }}</h2>
        <p class="sub">进入 API 开放平台管理后台</p>

        <el-form v-if="mode === 'login'" label-position="top" @submit.prevent="onLogin">
          <el-form-item label="用户名">
            <el-input v-model="loginForm.username" placeholder="请输入用户名" size="large" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" size="large" />
          </el-form-item>
          <el-button type="primary" size="large" class="full" :loading="loading" native-type="submit">
            登 录
          </el-button>
        </el-form>

        <el-form v-else label-position="top" @submit.prevent="onRegister">
          <el-form-item label="用户名">
            <el-input v-model="registerForm.username" placeholder="3~32 位" size="large" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="registerForm.nickname" placeholder="可选" size="large" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" size="large" />
          </el-form-item>
          <p class="hint">用户名注册为 admin 时自动授予管理员，进入管理控制台；其他用户名进入开发者工作台</p>
          <el-button type="primary" size="large" class="full" :loading="loading" native-type="submit">
            注 册
          </el-button>
        </el-form>

        <button class="switch" type="button" @click="mode = mode === 'login' ? 'register' : 'login'">
          {{ mode === "login" ? "没有账号？去注册" : "已有账号？去登录" }}
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  position: relative;
  overflow: hidden;
}

.glow {
  position: absolute;
  width: 480px;
  height: 480px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(37, 99, 235, 0.18), transparent 70%);
  left: 12%;
  top: 18%;
  pointer-events: none;
}

.brand-panel {
  padding: 64px 72px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  z-index: 1;
}

.brand {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 56px;
}

.logo {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--primary);
  display: grid;
  place-items: center;
  font-weight: 700;
}

.brand h1 {
  margin: 0 0 4px;
  font-size: 28px;
}

.brand p {
  margin: 0;
  color: var(--text-secondary);
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

li strong {
  display: block;
  margin-bottom: 4px;
}

li span {
  color: var(--text-secondary);
  font-size: 13px;
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.form-card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
}

.form-card h2 {
  margin: 0 0 6px;
}

.sub,
.hint {
  color: var(--text-muted);
  font-size: 13px;
}

.sub {
  margin: 0 0 24px;
}

.full {
  width: 100%;
  margin-top: 8px;
}

.switch {
  margin-top: 18px;
  border: none;
  background: none;
  color: var(--accent);
  cursor: pointer;
  font-size: 13px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }
  .brand-panel {
    display: none;
  }
}
</style>
