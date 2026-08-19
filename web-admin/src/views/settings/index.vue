<script setup lang="ts">
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import PageHeader from "@/components/PageHeader.vue";

const userStore = useUserStore();

const demoUsers = [
  { username: "admin", nickname: "平台管理员", role: "管理员", status: "正常" },
  { username: "test01", nickname: "开发者甲", role: "开发者", status: "正常" },
];

function save() {
  ElMessage.success("演示环境：设置已保存到本地视图");
}

function resetQuota() {
  ElMessage.success("演示环境：已重置演示配额，待后端接入后生效");
}
</script>

<template>
  <div class="page">
    <PageHeader title="系统设置" desc="仅管理员可访问：账号、用户与控制台偏好" />
    <div class="settings-grid">
      <div class="card block">
        <h3>当前账号</h3>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ userStore.profile?.username || "-" }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ userStore.profile?.nickname || "-" }}</el-descriptions-item>
          <el-descriptions-item label="用户 ID">{{ userStore.profile?.userId || "-" }}</el-descriptions-item>
          <el-descriptions-item label="角色">{{ userStore.rolesText }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card block">
        <h3>控制台偏好</h3>
        <el-form label-position="top">
          <el-form-item label="主题">
            <el-select model-value="dark" disabled style="width: 220px">
              <el-option label="深色科技风" value="dark" />
            </el-select>
          </el-form-item>
          <el-form-item label="语言">
            <el-select model-value="zh-CN" disabled style="width: 220px">
              <el-option label="简体中文" value="zh-CN" />
            </el-select>
          </el-form-item>
          <el-button type="primary" @click="save">保存</el-button>
        </el-form>
      </div>
    </div>

    <div class="card block users">
      <div class="users-head">
        <h3>用户与角色（前端演示）</h3>
        <el-button size="small" @click="resetQuota">重置演示配额</el-button>
      </div>
      <p class="hint">此表暂不对接后端，仅用于管理员页面与普通用户页面的视觉区分。</p>
      <el-table :data="demoUsers" size="small">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="nickname" label="昵称" />
        <el-table-column prop="role" label="角色" />
        <el-table-column prop="status" label="状态" />
      </el-table>
    </div>
  </div>
</template>

<style scoped>
.settings-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}
.block {
  padding: 20px 22px 24px;
}
.block h3 {
  margin: 0 0 16px;
  font-size: 15px;
}
.users {
  margin-top: 16px;
}
.users-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.users-head h3 {
  margin: 0;
}
.hint {
  margin: 0 0 14px;
  font-size: 12px;
  color: var(--text-muted);
}
@media (max-width: 900px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>

