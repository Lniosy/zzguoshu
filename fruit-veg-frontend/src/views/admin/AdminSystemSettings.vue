<template>
  <div class="settings-page">
    <el-card class="header-card">
      <div class="title-wrap">
        <div>
          <h2>系统参数设置</h2>
          <p>维护平台基础信息和业务参数，修改后立即生效</p>
        </div>
        <el-button type="primary" :loading="saving" @click="save">保存设置</el-button>
      </div>
    </el-card>

    <el-row :gutter="16">
      <el-col :span="24">
        <el-card class="panel">
          <template #header>平台信息</template>
          <el-form label-width="110px">
            <el-form-item label="平台名称">
              <el-input v-model="form.platformName" placeholder="请输入平台名称" />
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="form.contactPhone" placeholder="请输入客服电话" />
            </el-form-item>
            <el-form-item label="联系邮箱">
              <el-input v-model="form.contactEmail" placeholder="请输入联系邮箱" />
            </el-form-item>
            <el-form-item label="ICP备案号">
              <el-input v-model="form.icpNo" placeholder="请输入备案号" />
            </el-form-item>
            <el-form-item label="版权信息">
              <el-input v-model="form.copyright" placeholder="请输入版权信息" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getSystemSettings, updateSystemSettings } from '@/api/admin'

const saving = ref(false)
const form = reactive({
  platformName: '',
  contactPhone: '',
  contactEmail: '',
  icpNo: '',
  copyright: '',
  orderAutoCancelMinutes: 30,
  afterSaleExpireDays: 7,
  productAuditRequired: true,
  traceAuditRequired: false
})

const load = async () => {
  const data = await getSystemSettings()
  Object.assign(form, data || {})
}

const save = async () => {
  saving.value = true
  try {
    await updateSystemSettings({ ...form })
    ElMessage.success('系统参数已保存')
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.settings-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.header-card {
  background: linear-gradient(120deg, #f4f9ff 0%, #eef8f0 100%);
}
.title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.title-wrap h2 {
  margin: 0 0 6px;
  color: #233329;
}
.title-wrap p {
  margin: 0;
  color: #5f7168;
}
.panel {
  min-height: 320px;
}
</style>
