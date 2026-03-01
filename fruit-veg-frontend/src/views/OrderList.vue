<template>
  <div class="order-list-container">
    <el-container class="order-list">
      <el-header class="order-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>我的订单</h2>
        </div>
      </el-header>
      <el-main class="order-main">
        <!-- 订单筛选 -->
        <el-card class="filter-card">
          <el-row :gutter="10">
            <el-col :span="6">
              <el-select v-model="filterParams.status" placeholder="订单状态" @change="handleFilter">
                <el-option label="全部" value="" />
                <el-option label="待支付" value="pending" />
                <el-option label="待发货" value="shipped" />
                <el-option label="待收货" value="delivered" />
                <el-option label="已完成" value="completed" />
                <el-option label="售后处理中" value="refunding" />
                <el-option label="已取消" value="cancelled" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="filterParams.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="handleFilter"
              />
            </el-col>
            <el-col :span="6">
              <el-input
                v-model="filterParams.orderNumber"
                placeholder="订单号"
                @keyup.enter="handleFilter"
              >
                <template #append>
                  <el-button @click="handleFilter">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </el-col>
            <el-col :span="6">
              <el-button type="primary" @click="handleReset">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-col>
          </el-row>
        </el-card>
        <!-- 订单列表 -->
        <el-card class="order-card">
          <template #header>
            <h3>订单列表</h3>
          </template>
          <el-table
            :data="orderList"
            style="width: 100%"
            v-loading="loading"
          >
            <el-table-column prop="orderNumber" label="订单号" width="180" />
            <el-table-column prop="totalAmount" label="订单金额" width="120">
              <template #default="scope">
                ¥{{ scope.row.totalAmount.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="订单状态" width="120">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                {{ formatDate(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="deliveryTime" label="发货时间" width="180">
              <template #default="scope">
                {{ scope.row.deliveryTime ? formatDate(scope.row.deliveryTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="receiveTime" label="收货时间" width="180">
              <template #default="scope">
                {{ scope.row.receiveTime ? formatDate(scope.row.receiveTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button link @click="handleView(scope.row)">
                  <el-icon><View /></el-icon>
                  查看详情
                </el-button>
                <el-button
                  link
                  @click="handleCancel(scope.row)"
                  v-if="scope.row.status === 'pending'"
                >
                  <el-icon><Delete /></el-icon>
                  取消订单
                </el-button>
                <el-button
                  link
                  @click="handlePay(scope.row)"
                  v-if="scope.row.status === 'pending'"
                >
                  <el-icon><CreditCard /></el-icon>
                  立即支付
                </el-button>
                <el-button
                  link
                  @click="handleConfirmReceive(scope.row)"
                  v-if="scope.row.status === 'delivered'"
                >
                  <el-icon><CircleCheck /></el-icon>
                  确认收货
                </el-button>
                <el-button
                  link
                  @click="handleRemindShip(scope.row)"
                  v-if="scope.row.status === 'shipped'"
                >
                  <el-icon><Refresh /></el-icon>
                  提醒发货
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <!-- 分页 -->
          <el-pagination
            v-model:current-page="pageParams.current"
            v-model:page-size="pageParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="pageParams.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </el-card>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ArrowLeft, Search, Refresh, View, Delete, CreditCard, CircleCheck } from '@element-plus/icons-vue'
import { cancelOrder, confirmReceive, getOrderList, payOrder } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const userStore = useUserStore()

// 响应式数据
const orderList = ref([])
const loading = ref(false)
const filterParams = reactive({
  status: '',
  dateRange: [],
  orderNumber: ''
})
const pageParams = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 方法
const navigateBack = () => {
  navigate('/user')
}

const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    shipped: 'info',
    delivered: 'primary',
    completed: 'success',
    refunding: 'warning',
    cancelled: 'danger'
  }
  return typeMap[status] || 'default'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待支付',
    shipped: '待发货',
    delivered: '待收货',
    completed: '已完成',
    refunding: '售后处理中',
    cancelled: '已取消'
  }
  return textMap[status] || '未知'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return d.toLocaleString()
}

const handleFilter = () => {
  pageParams.current = 1
  fetchOrderList()
}

const handleReset = () => {
  filterParams.status = ''
  filterParams.dateRange = []
  filterParams.orderNumber = ''
  pageParams.current = 1
  fetchOrderList()
}

const handleSizeChange = (size) => {
  pageParams.pageSize = size
  fetchOrderList()
}

const handleCurrentChange = (current) => {
  pageParams.current = current
  fetchOrderList()
}

const handleView = (order) => {
  navigate(`/orders/${order.id}`)
}

const handleCancel = async (order) => {
  await cancelOrder(order.id)
  ElMessage.success('订单已取消')
  fetchOrderList()
}

const handlePay = async (order) => {
  await payOrder(order.id, { paymentMethod: '微信支付' })
  ElMessage.success('支付成功')
  fetchOrderList()
}

const handleConfirmReceive = async (order) => {
  await confirmReceive(order.id)
  ElMessage.success('确认收货成功')
  fetchOrderList()
}

const handleRemindShip = async () => {
  ElMessage.success('已提醒商家尽快发货')
}

const fetchOrderList = async () => {
  if (!userStore.getIsLoggedIn) {
    ElMessage.warning('请先登录')
    navigate('/login')
    return
  }
  loading.value = true
  try {
    const params = {
      page: pageParams.current,
      pageSize: pageParams.pageSize,
      ...filterParams
    }
    const data = await getOrderList(params)
    orderList.value = data.records
    pageParams.total = data.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

// 钩子函数
onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
.order-list-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.order-list {
  min-height: 100vh;
}

.order-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 0;
}

.header-content {
  display: flex;
  align-items: center;
  height: 60px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.header-content h2 {
  margin: 0 0 0 20px;
}

.order-main {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.order-card {
  margin-bottom: 20px;
}

.order-card :deep(.el-table) {
  margin-bottom: 20px;
}
</style>
