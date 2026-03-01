<template>
  <div class="admin-role-page">
    <el-card>
      <template #header>
        <div class="head-row">
          <h3>角色权限管理</h3>
          <el-tag type="info">菜单级 + 按钮级（基础版）</el-tag>
        </div>
      </template>
      <el-table :data="roles" v-loading="loading">
        <el-table-column prop="role" label="角色" width="140" />
        <el-table-column prop="permissionCount" label="权限数" width="100" />
        <el-table-column label="权限列表" min-width="360">
          <template #default="{ row }">
            <el-space wrap>
              <el-tag v-for="perm in row.permissions" :key="perm" size="small" effect="plain">{{ perm }}</el-tag>
            </el-space>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑权限</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="编辑角色权限" width="680px">
      <div class="role-title">角色：{{ form.role }}</div>
      <el-checkbox-group v-model="form.permissions">
        <el-space wrap>
          <el-checkbox v-for="perm in allPermissions" :key="perm" :label="perm">{{ perm }}</el-checkbox>
        </el-space>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getRoleList, updateRolePermissions } from '@/api/admin'

const loading = ref(false)
const saving = ref(false)
const visible = ref(false)
const roles = ref([])
const form = ref({ role: '', permissions: [] })
const allPermissions = ref([])

const load = async () => {
  loading.value = true
  try {
    const data = await getRoleList()
    roles.value = data || []
    allPermissions.value = Array.from(new Set((data || []).flatMap(item => item.permissions || [])))
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  form.value = {
    role: row.role,
    permissions: [...(row.permissions || [])]
  }
  visible.value = true
}

const submit = async () => {
  saving.value = true
  try {
    await updateRolePermissions(form.value.role, form.value.permissions)
    ElMessage.success('权限更新成功')
    visible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.head-row { display: flex; align-items: center; justify-content: space-between; }
.head-row h3 { margin: 0; }
.role-title { margin-bottom: 10px; color: #344c3e; font-weight: 600; }
</style>

