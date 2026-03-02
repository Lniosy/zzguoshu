<template>
  <div class="after-sale-page">
    <div class="page-header">
      <h2>我的售后</h2>
      <el-button link @click="navigate('/user')">返回用户中心</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="售后单号" width="120" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="reason" label="原因" min-width="220" show-overflow-tooltip />
        <el-table-column prop="statusText" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'handled' ? 'success' : 'warning'">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" width="170" />
        <el-table-column prop="handleTime" label="处理时间" width="170" />
        <el-table-column label="处理结果" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.handleResult || '-' }}</template>
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
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { getAfterSaleList } from '@/api/order'

const router = useRouter()
const { navigate } = useAppNavigation()
const loading = ref(false)
const list = ref([])
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getAfterSaleList({
      page: pagination.current,
      pageSize: pagination.pageSize
    })
    list.value = data.records || []
    pagination.total = data.total || 0
  } finally {
    loading.value = false
  }
}

fetchList()
</script>

<style scoped>
.after-sale-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
.page-header h2 { margin: 0; }
.pagination { margin-top: 14px; text-align: right; }
</style>
