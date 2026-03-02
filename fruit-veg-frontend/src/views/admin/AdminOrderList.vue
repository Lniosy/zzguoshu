<template>
  <div class="admin-order-list">
    <el-card class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input
            v-model="searchForm.orderNo"
            placeholder="请输入订单号"
            prefix-icon="Document"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="用户手机号">
          <el-input
            v-model="searchForm.phone"
            placeholder="请输入用户手机号"
            prefix-icon="Phone"
            :clearable="true"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="searchForm.status"
            placeholder="请选择订单状态"
            prefix-icon="CircleCheck"
            :clearable="true"
            style="width: 120px"
          >
            <el-option label="待付款" :value="0" />
            <el-option label="待发货" :value="1" />
            <el-option label="待收货" :value="2" />
            <el-option label="待评价" :value="3" />
            <el-option label="已完成" :value="4" />
            <el-option label="已取消" :value="5" />
            <el-option label="售后处理中" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="order-card">
      <el-table
        :data="orderList"
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="phone" label="用户手机号" min-width="130" />
        <el-table-column prop="totalAmount" label="订单金额" width="120">
          <template #default="{ row }">
            ¥{{ row.totalAmount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="订单状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payTime" label="支付时间" min-width="160" />
        <el-table-column prop="createTime" label="创建时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              :link="true"
              @click="handleShip(row)"
              v-if="row.status === 1"
            >
              发货
            </el-button>
            <el-button link :link="true" @click="handleView(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { getOrderList, shipOrder } from '@/api/admin'
import { ElMessage, ElMessageBox } from 'element-plus'

// 响应式数据
const loading = ref(false)
const orderList = ref([])
const searchForm = reactive({
  orderNo: '',
  phone: '',
  status: ''
})
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})
const selectedOrders = ref([])

// 方法
const fetchOrderList = async () => {
  loading.value = true
  try {
    const params = {
      ...searchForm,
      page: pagination.current,
      pageSize: pagination.pageSize
    }
    const response = await getOrderList(params)
    orderList.value = response.records
    pagination.total = response.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.current = 1
  fetchOrderList()
}

const handleReset = () => {
  searchForm.orderNo = ''
  searchForm.phone = ''
  searchForm.status = ''
  pagination.current = 1
  fetchOrderList()
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待付款',
    1: '待发货',
    2: '待收货',
    3: '待评价',
    4: '已完成',
    5: '已取消',
    6: '售后处理中'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'info',
    2: 'primary',
    3: 'success',
    4: 'success',
    5: 'danger',
    6: 'warning'
  }
  return typeMap[status] || 'info'
}

const handleShip = async (row) => {
  try {
    await shipOrder(row.id, {
      logisticsCompany: '顺丰快递',
      logisticsNo: 'SF1234567890'
    })
    row.status = 2
    ElMessage.success('订单已发货')
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.error('发货失败')
  }
}

const handleView = (row) => {
  window.open(`/orders/${row.id}`, '_blank')
}

const handleSelectionChange = (selection) => {
  selectedOrders.value = selection
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  fetchOrderList()
}

const handleCurrentChange = (current) => {
  pagination.current = current
  fetchOrderList()
}

// 初始化
fetchOrderList()
</script>

<style scoped>
.admin-order-list {
  padding: 0;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: end;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}
</style>
