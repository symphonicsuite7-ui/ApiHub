<script setup lang="ts">
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import PageHeader from "@/components/PageHeader.vue";

const userStore = useUserStore();

function save() {
  ElMessage.success("演示环境：设置已保存到本地视图");
}
</script>

<template>
  <div class="page">
    <PageHeader title="系统设置" desc="账号信息与控制台偏好，后续可接入改密接口" />
    <div class="settings-grid">
      <div class="card block">
        <h3>账号信息</h3>
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
@media (max-width: 900px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>
