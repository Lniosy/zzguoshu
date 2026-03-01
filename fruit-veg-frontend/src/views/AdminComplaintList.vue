<template>
  <div class="admin-complaint-list">
    <el-card class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="处理状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待处理" value="pending" />
            <el-option label="已处理" value="handled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="售后单ID" width="110" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="reason" label="申请原因" min-width="220" show-overflow-tooltip />
        <el-table-column prop="statusText" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'handled' ? 'success' : 'warning'">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column prop="handleTime" label="处理时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link @click="viewDetail(row)">详情</el-button>
            <el-button v-if="row.status === 'pending'" link type="primary" @click="openHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @current-change="fetchList"
        @size-change="fetchList"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="售后详情" width="560px">
      <el-descriptions :column="1" border v-if="activeRow">
        <el-descriptions-item label="售后单ID">{{ activeRow.id }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ activeRow.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="售后类型">{{ activeRow.type }}</el-descriptions-item>
        <el-descriptions-item label="申请原因">{{ activeRow.reason }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">{{ activeRow.statusText }}</el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ activeRow.handleResult || '-' }}</el-descriptions-item>
        <el-descriptions-item label="处理备注">{{ activeRow.handleRemark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="handleVisible" title="处理售后申请" width="520px">
      <el-form :model="handleForm" label-width="88px">
        <el-form-item label="处理结果">
          <el-select v-model="handleForm.result" style="width: 100%">
            <el-option label="同意退款" value="同意退款" />
            <el-option label="拒绝退款" value="拒绝退款" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.remark" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="handling" @click="submitHandle">提交处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getComplaintList, handleComplaint } from '@/api/admin'

const loading = ref(false)
const list = ref([])
const detailVisible = ref(false)
const handleVisible = ref(false)
const activeRow = ref(null)
const handling = ref(false)

const searchForm = reactive({
  orderNo: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const handleForm = reactive({
  result: '同意退款',
  remark: ''
})

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getComplaintList({
      ...searchForm,
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    list.value = data.records || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchList()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.status = ''
  pagination.current = 1
  fetchList()
}

const viewDetail = (row) => {
  activeRow.value = row
  detailVisible.value = true
}

const openHandle = (row) => {
  activeRow.value = row
  handleForm.result = '同意退款'
  handleForm.remark = ''
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!activeRow.value) return
  handling.value = true
  try {
    await handleComplaint(activeRow.value.id, {
      result: handleForm.result,
      remark: handleForm.remark
    })
    ElMessage.success('处理成功')
    handleVisible.value = false
    fetchList()
  } finally {
    handling.value = false
  }
}

fetchList()
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.search-form { display: flex; gap: 10px; align-items: end; flex-wrap: wrap; }
.pagination { margin-top: 16px; text-align: right; }
</style>
