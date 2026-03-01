<template>
  <div class="order-detail-container">
    <el-container class="order-detail">
      <el-header class="order-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>订单详情</h2>
        </div>
      </el-header>
      <el-main class="order-main">
        <!-- 订单基本信息 -->
        <el-card class="order-info-card">
          <template #header>
            <h3>订单信息</h3>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ order.orderNumber }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ order.totalAmount.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="支付方式">{{ order.paymentMethod }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(order.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ order.paymentTime ? formatDate(order.paymentTime) : '-' }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ order.deliveryTime ? formatDate(order.deliveryTime) : '-' }}</el-descriptions-item>
            <el-descriptions-item label="收货时间">{{ order.receiveTime ? formatDate(order.receiveTime) : '-' }}</el-descriptions-item>
            <el-descriptions-item label="评价状态">
              <el-tag :type="order.reviewed ? 'success' : 'info'">{{ order.reviewed ? '已评价' : '未评价' }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
        <!-- 收货地址 -->
        <el-card class="address-card">
          <template #header>
            <h3>收货地址</h3>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="收货人">{{ order.receiverName }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ order.receiverPhone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ order.address }}</el-descriptions-item>
            <el-descriptions-item label="物流公司">{{ order.logisticsCompany || '-' }}</el-descriptions-item>
            <el-descriptions-item label="物流单号">{{ order.logisticsNo || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <!-- 商品列表 -->
        <el-card class="product-card">
          <template #header>
            <h3>商品列表</h3>
          </template>
          <el-table
            :data="order.products"
            style="width: 100%"
            border
          >
            <el-table-column prop="name" label="商品名称" min-width="200">
              <template #default="scope">
                <div class="product-info">
                  <el-image
                    :src="scope.row.images[0]"
                    fit="cover"
                    style="width: 60px; height: 60px"
                    preview-teleported
                  >
                    <template #error>
                      <div class="image-error">
                        <el-icon><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="product-details">
                    <h4>{{ scope.row.name }}</h4>
                    <p v-if="scope.row.spec">{{ scope.row.spec }}</p>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="单价">
              <template #default="scope">
                ¥{{ scope.row.price.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column label="小计" width="120">
              <template #default="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="溯源" width="120">
              <template #default="scope">
                <el-button link type="primary" @click="viewTrace(scope.row)">查看溯源</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
        <el-card class="logistics-card">
          <template #header><h3>物流轨迹</h3></template>
          <el-timeline v-if="order.logisticsTracks?.length">
            <el-timeline-item
              v-for="(track, idx) in order.logisticsTracks"
              :key="idx"
              :timestamp="formatDate(track.time)"
              placement="top"
            >
              <b>{{ track.status }}</b>：{{ track.description }}
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无物流信息" />
        </el-card>
        <!-- 订单操作 -->
        <el-card class="action-card" v-if="showActions">
          <template #header>
            <h3>订单操作</h3>
          </template>
          <div class="order-actions">
            <el-button
              type="primary"
              @click="handleCancel"
              v-if="order.status === 'pending'"
            >
              <el-icon><Delete /></el-icon>
              取消订单
            </el-button>
            <el-button
              type="success"
              @click="handlePay"
              v-if="order.status === 'pending'"
            >
              <el-icon><CreditCard /></el-icon>
              立即支付
            </el-button>
            <el-button
              type="primary"
              @click="handleConfirmReceive"
              v-if="order.status === 'delivered'"
            >
              <el-icon><CircleCheck /></el-icon>
              确认收货
            </el-button>
            <el-button
              type="default"
              @click="handleRefund"
              v-if="order.status === 'completed' || order.status === 'delivered'"
            >
              <el-icon><Refresh /></el-icon>
              申请退款
            </el-button>
            <el-button
              type="warning"
              @click="handleReview"
              v-if="order.status === 'completed' && !order.reviewed"
            >
              <el-icon><Star /></el-icon>
              评价订单
            </el-button>
          </div>
        </el-card>
      </el-main>
    </el-container>

    <el-dialog v-model="refundVisible" title="申请售后" width="520px">
      <el-form :model="refundForm" label-width="90px">
        <el-form-item label="售后类型">
          <el-select v-model="refundForm.type" style="width: 100%">
            <el-option label="仅退款" value="仅退款" />
            <el-option label="退货退款" value="退货退款" />
          </el-select>
        </el-form-item>
        <el-form-item label="申请原因">
          <el-input v-model="refundForm.reason" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRefund">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewVisible" title="订单评价" width="560px">
      <el-form :model="reviewForm" label-width="90px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="4" maxlength="300" show-word-limit />
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="reviewForm.anonymous" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ArrowLeft, Picture, Delete, CreditCard, CircleCheck, Refresh, Star } from '@element-plus/icons-vue'
import { applyRefund, cancelOrder, confirmReceive, getOrderDetail, payOrder, submitOrderReview } from '@/api/order'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const { navigate } = useAppNavigation()
const route = useRoute()
const userStore = useUserStore()

// 响应式数据
const order = ref({
  id: 0,
  orderNumber: '',
  totalAmount: 0,
  status: '',
  paymentMethod: '',
  createTime: '',
  paymentTime: '',
  deliveryTime: '',
  receiveTime: '',
  receiverName: '',
  receiverPhone: '',
  address: '',
  logisticsCompany: '',
  logisticsNo: '',
  logisticsTracks: [],
  products: []
})
const showActions = ref(true)
const refundVisible = ref(false)
const reviewVisible = ref(false)
const refundForm = ref({
  type: '仅退款',
  reason: '商品与描述不符'
})
const reviewForm = ref({
  rating: 5,
  content: '',
  anonymous: false
})

// 方法
const navigateBack = () => {
  navigate('/orders')
}

const viewTrace = (row) => {
  if (!row?.id) {
    ElMessage.info('该商品暂无溯源档案')
    return
  }
  navigate(`/products/${row.id}/trace`)
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
  return typeMap[status] || 'info'
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

const handleCancel = async () => {
  await cancelOrder(order.value.id)
  ElMessage.success('订单已取消')
  fetchOrderDetail()
}

const handlePay = async () => {
  await payOrder(order.value.id, { paymentMethod: '微信支付' })
  ElMessage.success('支付成功')
  fetchOrderDetail()
}

const handleConfirmReceive = async () => {
  await confirmReceive(order.value.id)
  ElMessage.success('确认收货成功')
  fetchOrderDetail()
}

const handleRefund = async () => {
  refundVisible.value = true
}

const handleReview = () => {
  reviewForm.value = {
    rating: 5,
    content: '',
    anonymous: false
  }
  reviewVisible.value = true
}

const submitRefund = async () => {
  await applyRefund(order.value.id, {
    type: refundForm.value.type,
    reason: refundForm.value.reason
  })
  ElMessage.success('已提交售后申请')
  refundVisible.value = false
  fetchOrderDetail()
}

const submitReview = async () => {
  await submitOrderReview(order.value.id, {
    rating: reviewForm.value.rating,
    content: reviewForm.value.content,
    anonymous: reviewForm.value.anonymous,
    products: (order.value.products || []).map(item => ({
      productId: item.id,
      productName: item.name,
      rating: reviewForm.value.rating
    }))
  })
  ElMessage.success('评价提交成功')
  reviewVisible.value = false
  fetchOrderDetail()
}

const fetchOrderDetail = async () => {
  const orderId = route.params.id
  if (!orderId) {
    ElMessage.error('订单ID无效')
    navigate('/orders')
    return
  }
  try {
    const data = await getOrderDetail(orderId)
    order.value = data
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

// 钩子函数
onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.order-detail {
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

.order-info-card,
.address-card,
.product-card,
.logistics-card,
.action-card {
  margin-bottom: 20px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-details h4 {
  margin: 0 0 5px 0;
  font-size: 14px;
  font-weight: 600;
}

.product-details p {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.order-actions {
  display: flex;
  gap: 10px;
}
</style>
