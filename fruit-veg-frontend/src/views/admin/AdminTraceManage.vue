<template>
  <div class="admin-trace-manage">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="模板管理" name="template">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>溯源模板</span>
              <el-button type="primary" @click="openTemplateDialog()">新建模板</el-button>
            </div>
          </template>
          <el-table :data="templateList" border>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="模板名称" min-width="160" />
            <el-table-column prop="description" label="说明" min-width="240" show-overflow-tooltip />
            <el-table-column prop="fields" label="字段" min-width="260">
              <template #default="{ row }">
                <el-tag v-for="field in row.fields || []" :key="field" size="small" class="field-tag">{{ field }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link @click="openTemplateDialog(row)">编辑</el-button>
                <el-button link type="danger" @click="removeTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="数据审核" name="audit">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>溯源审核</span>
              <el-form :inline="true" :model="searchForm" class="filter-form">
                <el-form-item>
                  <el-input v-model="searchForm.keyword" placeholder="商品/商家关键词" clearable />
                </el-form-item>
                <el-form-item>
                  <el-select v-model="searchForm.auditStatus" placeholder="审核状态" clearable style="width: 120px">
                    <el-option label="待审核" :value="0" />
                    <el-option label="已通过" :value="1" />
                    <el-option label="已驳回" :value="2" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="fetchTraceList">查询</el-button>
                </el-form-item>
              </el-form>
            </div>
          </template>

          <el-table :data="traceList" border v-loading="traceLoading">
            <el-table-column prop="productId" label="商品ID" width="90" />
            <el-table-column prop="productName" label="商品" min-width="140" />
            <el-table-column prop="merchantName" label="商家" min-width="130" />
            <el-table-column prop="originName" label="产地" min-width="130" />
            <el-table-column prop="plantTime" label="种植时间" width="170" />
            <el-table-column prop="harvestTime" label="采收时间" width="170" />
            <el-table-column prop="auditStatus" label="审核状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType(row.auditStatus)">{{ statusText(row.auditStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="auditRemark" label="审核备注" min-width="180" show-overflow-tooltip />
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button link type="success" @click="auditTrace(row, 1)">通过</el-button>
                <el-button link type="danger" @click="auditTrace(row, 2)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            layout="total, prev, pager, next"
            class="pagination"
            @current-change="fetchTraceList"
          />
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="覆盖统计" name="stats">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-card class="stats-card">
              <div class="label">商品总数</div>
              <div class="value">{{ stats.totalProductCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stats-card">
              <div class="label">已录入溯源</div>
              <div class="value">{{ stats.traceCoverageCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stats-card">
              <div class="label">覆盖率</div>
              <div class="value">{{ toPercent(stats.traceCoverageRate) }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stats-card">
              <div class="label">待审核</div>
              <div class="value">{{ stats.pendingAuditCount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16" style="margin-top: 16px">
          <el-col :span="12">
            <el-card class="stats-card compact">
              <div class="label">审核通过</div>
              <div class="value success">{{ stats.approvedCount || 0 }}</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="stats-card compact">
              <div class="label">审核驳回</div>
              <div class="value danger">{{ stats.rejectedCount || 0 }}</div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="templateDialogVisible" :title="templateForm.id ? '编辑模板' : '新建模板'" width="560px">
      <el-form :model="templateForm" label-width="90px">
        <el-form-item label="模板名称">
          <el-input v-model="templateForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="模板说明">
          <el-input v-model="templateForm.description" type="textarea" :rows="3" maxlength="120" />
        </el-form-item>
        <el-form-item label="字段列表">
          <el-select
            v-model="templateForm.fields"
            multiple
            allow-create
            default-first-option
            filterable
            placeholder="输入后回车添加字段"
            style="width: 100%"
          >
            <el-option v-for="field in defaultFields" :key="field" :label="field" :value="field" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  auditAdminTrace,
  deleteTraceTemplate,
  getAdminTraceList,
  getAdminTraceStats,
  getTraceTemplateList,
  saveTraceTemplate
} from '@/api/admin'

const activeTab = ref('template')

const templateList = ref([])
const templateDialogVisible = ref(false)
const templateForm = reactive({
  id: null,
  name: '',
  description: '',
  fields: []
})
const defaultFields = [
  'originName',
  'originAddress',
  'plantMethod',
  'plantTime',
  'harvestTime',
  'storageCondition',
  'shelfLife',
  'testReport'
]

const traceLoading = ref(false)
const traceList = ref([])
const searchForm = reactive({
  keyword: '',
  auditStatus: ''
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const stats = ref({})

const statusText = (status) => {
  const map = { 0: '待审核', 1: '已通过', 2: '已驳回' }
  return map[status] || '未知'
}

const statusType = (status) => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[status] || 'info'
}

const toPercent = (value) => `${Math.round((Number(value || 0) * 10000)) / 100}%`

const fetchTemplateList = async () => {
  const data = await getTraceTemplateList()
  templateList.value = data.records || []
}

const fetchTraceList = async () => {
  traceLoading.value = true
  try {
    const data = await getAdminTraceList({
      page: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword,
      auditStatus: searchForm.auditStatus === '' ? undefined : searchForm.auditStatus
    })
    traceList.value = data.records || []
    pagination.total = data.total || 0
  } finally {
    traceLoading.value = false
  }
}

const fetchStats = async () => {
  stats.value = await getAdminTraceStats()
}

const openTemplateDialog = (row) => {
  templateForm.id = row?.id || null
  templateForm.name = row?.name || ''
  templateForm.description = row?.description || ''
  templateForm.fields = row?.fields ? [...row.fields] : [...defaultFields]
  templateDialogVisible.value = true
}

const submitTemplate = async () => {
  if (!templateForm.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }
  await saveTraceTemplate({
    id: templateForm.id,
    name: templateForm.name.trim(),
    description: templateForm.description?.trim(),
    fields: templateForm.fields
  })
  ElMessage.success('模板已保存')
  templateDialogVisible.value = false
  fetchTemplateList()
}

const removeTemplate = async (row) => {
  await ElMessageBox.confirm(`将删除模板“${row.name}”，请确认`, '提示', { type: 'warning' })
  await deleteTraceTemplate(row.id)
  ElMessage.success('模板已删除')
  fetchTemplateList()
}

const auditTrace = async (row, status) => {
  const isReject = status === 2
  let remark = ''
  if (isReject) {
    remark = await ElMessageBox.prompt('请输入驳回原因', '驳回溯源', { inputPlaceholder: '例如：检测报告缺失' })
      .then(({ value }) => value || '')
      .catch(() => null)
    if (remark === null) return
  }
  await auditAdminTrace(row.productId, {
    status,
    remark: remark || (status === 1 ? '审核通过' : '信息不完整')
  })
  ElMessage.success(status === 1 ? '审核通过' : '已驳回')
  await Promise.all([fetchTraceList(), fetchStats()])
}

onMounted(async () => {
  await Promise.all([fetchTemplateList(), fetchTraceList(), fetchStats()])
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.filter-form {
  margin-left: auto;
}

.field-tag {
  margin: 0 6px 6px 0;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.stats-card {
  border-radius: 12px;
}

.stats-card .label {
  color: #68727d;
  font-size: 13px;
}

.stats-card .value {
  margin-top: 8px;
  color: #1e2a34;
  font-size: 28px;
  font-weight: 700;
}

.stats-card.compact .value {
  font-size: 24px;
}

.value.success {
  color: #1f8f4a;
}

.value.danger {
  color: #cd3d3d;
}
</style>
