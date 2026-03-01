<template>
  <div class="shop-manage-container">
    <el-container class="shop-manage">
      <el-header class="shop-header">
        <div class="header-content">
          <el-button link @click="navigateBack">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <h2>店铺运营中心</h2>
        </div>
      </el-header>
      <el-main class="shop-main">
        <el-tabs>
          <el-tab-pane label="店铺信息">
            <el-card class="shop-info-card">
              <template #header>
                <h3>店铺基本信息</h3>
              </template>

              <el-descriptions :column="2" border>
                <el-descriptions-item label="店铺名称">{{ shopInfo?.shopName || '-' }}</el-descriptions-item>
                <el-descriptions-item label="经营类型">{{ businessTypeMap[shopInfo?.businessType] || '-' }}</el-descriptions-item>
                <el-descriptions-item label="店铺简介">{{ shopInfo?.shopDescription || '-' }}</el-descriptions-item>
                <el-descriptions-item label="联系电话">{{ shopInfo?.contactPhone || '-' }}</el-descriptions-item>
                <el-descriptions-item label="经营地址">{{ shopInfo?.address || '-' }}</el-descriptions-item>
                <el-descriptions-item label="店铺Logo">
                  <el-avatar v-if="shopInfo?.logoUrl" :size="50" :src="shopInfo.logoUrl">
                    {{ shopInfo?.shopName?.charAt(0)?.toUpperCase() }}
                  </el-avatar>
                  <span v-else>-</span>
                </el-descriptions-item>
              </el-descriptions>

              <div class="shop-actions">
                <el-button type="primary" @click="navigateToEdit">
                  <el-icon><Edit /></el-icon>
                  编辑店铺信息
                </el-button>
              </div>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="商品管理">
            <el-card>
              <template #header>
                <div class="pane-head">
                  <h3>商家商品管理</h3>
                  <el-button type="primary" @click="openProductDialog()">新增商品</el-button>
                </div>
              </template>
              <el-table :data="merchantProducts" v-loading="loadingProducts">
                <el-table-column prop="id" label="ID" width="80" />
                <el-table-column prop="name" label="商品名称" min-width="180" />
                <el-table-column prop="categoryName" label="分类" width="100" />
                <el-table-column prop="price" label="价格" width="100">
                  <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="stock" label="库存" width="90" />
                <el-table-column prop="auditStatus" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="row.auditStatus === 1 ? 'success' : 'info'">{{ row.auditStatus === 1 ? '上架' : '下架' }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openProductDialog(row)">编辑</el-button>
                    <el-button v-if="row.auditStatus === 1" link @click="toggleProduct(row, 0)">下架</el-button>
                    <el-button v-else link @click="toggleProduct(row, 1)">上架</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="果蔬圈管理">
            <el-card>
              <template #header>
                <div class="pane-head">
                  <h3>动态发布与管理</h3>
                  <el-button type="primary" @click="openCreateDialog">发布动态</el-button>
                </div>
              </template>

              <el-table :data="circlePosts" v-loading="loadingCircle">
                <el-table-column prop="title" label="标题" min-width="220" />
                <el-table-column prop="createTime" label="发布时间" width="180" />
                <el-table-column prop="viewCount" label="浏览" width="90" />
                <el-table-column prop="likeCount" label="点赞" width="90" />
                <el-table-column prop="commentCount" label="评论" width="90" />
                <el-table-column label="操作" width="220">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
                    <el-button link type="danger" @click="removePost(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="溯源维护">
            <el-card>
              <template #header>
                <div class="pane-head">
                  <h3>全流程溯源维护</h3>
                </div>
              </template>
              <el-table :data="merchantTraces" v-loading="loadingTraces">
                <el-table-column prop="productId" label="商品ID" width="90" />
                <el-table-column prop="productName" label="商品名称" min-width="160" />
                <el-table-column prop="originName" label="产地" min-width="120" />
                <el-table-column prop="plantMethod" label="种植方式" width="120" />
                <el-table-column prop="harvestTime" label="采收时间" min-width="160" />
                <el-table-column label="操作" width="220" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openTraceDialog(row)">编辑溯源</el-button>
                    <el-button link @click="showTraceQrcode(row)">溯源二维码</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="订单履约">
            <el-card>
              <template #header>
                <div class="pane-head">
                  <h3>订单发货管理</h3>
                  <el-tag type="info">商家可处理待发货订单</el-tag>
                </div>
              </template>
              <el-table :data="merchantOrders" v-loading="loadingOrders">
                <el-table-column prop="orderNumber" label="订单号" min-width="180" />
                <el-table-column prop="receiverName" label="收货人" width="110" />
                <el-table-column prop="receiverPhone" label="电话" width="130" />
                <el-table-column prop="totalAmount" label="金额" width="110">
                  <template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.status === 'shipped' ? 'info' : row.status === 'delivered' ? 'primary' : row.status === 'completed' ? 'success' : row.status === 'refunding' ? 'warning' : 'info'">
                      {{ statusTextMap[row.status] || row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="logisticsNo" label="物流单号" min-width="180" />
                <el-table-column label="操作" width="200" fixed="right">
                  <template #default="{ row }">
                    <el-button link @click="navigate(`/orders/${row.id}`)">查看详情</el-button>
                    <el-button v-if="row.status === 'shipped'" link type="primary" @click="openShipDialog(row)">去发货</el-button>
                    <el-tag v-else size="small">已处理</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-tab-pane>

          <el-tab-pane label="数据统计">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-card v-loading="loadingStats">
                  <div class="kpi-title">累计销售额</div>
                  <div class="kpi-value">¥{{ Number(merchantStats.totalSalesAmount || 0).toFixed(2) }}</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card v-loading="loadingStats">
                  <div class="kpi-title">订单量 / 已完成</div>
                  <div class="kpi-value">{{ merchantStats.orderCount || 0 }} / {{ merchantStats.completedOrderCount || 0 }}</div>
                </el-card>
              </el-col>
              <el-col :span="8">
                <el-card v-loading="loadingStats">
                  <div class="kpi-title">访客量 / 商品数</div>
                  <div class="kpi-value">{{ merchantStats.visitorCount || 0 }} / {{ merchantStats.productCount || 0 }}</div>
                </el-card>
              </el-col>
            </el-row>
            <el-card style="margin-top: 14px" v-loading="loadingStats">
              <template #header>
                <div class="pane-head"><h3>商品销量排行</h3></div>
              </template>
              <el-table :data="merchantStats.topProducts || []">
                <el-table-column prop="name" label="商品名称" min-width="180" />
                <el-table-column prop="sales" label="销量" width="120" />
                <el-table-column prop="stock" label="库存" width="120" />
              </el-table>
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-main>
    </el-container>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑动态' : '发布动态'" width="640px">
      <el-form :model="form" label-width="88px">
        <el-form-item label="动态标题">
          <el-input v-model="form.title" maxlength="30" show-word-limit placeholder="例如：今日采摘新鲜黄瓜" />
        </el-form-item>
        <el-form-item label="动态内容">
          <el-input v-model="form.content" type="textarea" :rows="5" maxlength="1000" show-word-limit placeholder="分享种植过程、农场环境或采摘记录" />
        </el-form-item>
        <el-form-item label="图片URL">
          <el-input v-model="imageInput" placeholder="输入图片 URL，多个请用英文逗号分隔" />
        </el-form-item>
        <el-form-item label="关联商品ID">
          <el-input v-model="productIdsInput" placeholder="多个商品 ID 用英文逗号分隔，如 1,3,4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePost" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="shipDialogVisible" title="订单发货" width="520px">
      <el-form :model="shipForm" label-width="90px">
        <el-form-item label="物流公司">
          <el-input v-model="shipForm.logisticsCompany" placeholder="例如：顺丰 / 京东" />
        </el-form-item>
        <el-form-item label="物流单号">
          <el-input v-model="shipForm.logisticsNo" placeholder="请输入物流单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="productVisible" :title="productForm.id ? '编辑商品' : '新增商品'" width="560px">
      <el-form :model="productForm" label-width="90px">
        <el-form-item label="商品名称"><el-input v-model="productForm.name" /></el-form-item>
        <el-form-item label="商品分类">
          <el-select v-model="productForm.categoryId" style="width: 100%">
            <el-option label="水果" :value="1" />
            <el-option label="蔬菜" :value="2" />
            <el-option label="有机食品" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格"><el-input-number v-model="productForm.price" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="原价"><el-input-number v-model="productForm.originalPrice" :min="0.01" :precision="2" /></el-form-item>
        <el-form-item label="库存"><el-input-number v-model="productForm.stock" :min="0" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="productForm.mainImage" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="productForm.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="productVisible = false">取消</el-button>
        <el-button type="primary" :loading="productSaving" @click="submitProduct">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="traceVisible" title="编辑溯源信息" width="620px">
      <el-form :model="traceForm" label-width="92px">
        <el-form-item label="产地"><el-input v-model="traceForm.originName" /></el-form-item>
        <el-form-item label="产地地址"><el-input v-model="traceForm.originAddress" /></el-form-item>
        <el-form-item label="种植方式"><el-input v-model="traceForm.plantMethod" /></el-form-item>
        <el-form-item label="种植时间"><el-input v-model="traceForm.plantTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="采收时间"><el-input v-model="traceForm.harvestTime" placeholder="yyyy-MM-dd HH:mm:ss" /></el-form-item>
        <el-form-item label="储存建议"><el-input v-model="traceForm.storageCondition" /></el-form-item>
        <el-form-item label="保质期(天)"><el-input-number v-model="traceForm.shelfLife" :min="1" /></el-form-item>
        <el-form-item label="检测报告"><el-input v-model="traceForm.testReport" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="traceVisible = false">取消</el-button>
        <el-button type="primary" :loading="traceSaving" @click="submitTrace">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="traceQrcodeVisible" title="溯源二维码" width="420px">
      <div class="qr-wrap" v-if="traceQrcode.url">
        <el-image :src="traceQrcode.url" fit="contain" style="width: 220px; height: 220px;" />
        <p>{{ traceQrcode.traceUrl }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Edit } from '@element-plus/icons-vue'
import { useMerchantStore } from '@/stores/merchant'
import {
  createMerchantCirclePost,
  deleteMerchantCirclePost,
  getMerchantCircleList,
  getMerchantProductList,
  getMerchantTraceList,
  getMerchantTraceQrcode,
  updateMerchantCirclePost,
  updateMerchantProductStatus,
  getMerchantOrderList,
  saveMerchantProduct,
  saveMerchantTrace,
  shipMerchantOrder,
  getMerchantStats
} from '@/api/merchant'

const router = useRouter()
const { navigate } = useAppNavigation()
const merchantStore = useMerchantStore()

const businessTypeMap = {
  FRUIT_WHOLESALE: '水果批发',
  VEGETABLE_WHOLESALE: '蔬菜批发',
  FRESH_RETAIL: '生鲜零售',
  GENERAL_STORE: '综合超市'
}

const shopInfo = computed(() => merchantStore.getShopInfo)
const circlePosts = ref([])
const loadingCircle = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const merchantProducts = ref([])
const loadingProducts = ref(false)
const productVisible = ref(false)
const productSaving = ref(false)
const merchantTraces = ref([])
const loadingTraces = ref(false)
const traceVisible = ref(false)
const traceSaving = ref(false)
const traceQrcodeVisible = ref(false)
const traceQrcode = ref({ url: '', traceUrl: '' })
const merchantOrders = ref([])
const loadingOrders = ref(false)
const loadingStats = ref(false)
const merchantStats = ref({
  totalSalesAmount: 0,
  orderCount: 0,
  completedOrderCount: 0,
  avgOrderAmount: 0,
  visitorCount: 0,
  productCount: 0,
  topProducts: []
})
const shipDialogVisible = ref(false)
const shipping = ref(false)
const shipForm = ref({
  orderId: null,
  logisticsCompany: '郑州冷链快配',
  logisticsNo: ''
})
const productForm = ref({
  id: null,
  name: '',
  categoryId: 1,
  price: 10,
  originalPrice: 12,
  stock: 100,
  mainImage: '',
  description: ''
})
const traceForm = ref({
  productId: null,
  originName: '',
  originAddress: '',
  plantMethod: '',
  plantTime: '',
  harvestTime: '',
  storageCondition: '',
  shelfLife: 5,
  testReport: ''
})

const form = ref({
  id: null,
  title: '',
  content: ''
})
const imageInput = ref('')
const productIdsInput = ref('')

const navigateBack = () => navigate('/user')
const navigateToEdit = () => navigate('/merchant/shop/edit')

const fetchCircle = async () => {
  loadingCircle.value = true
  try {
    const data = await getMerchantCircleList({ page: 1, size: 50 })
    circlePosts.value = data.records || []
  } finally {
    loadingCircle.value = false
  }
}

const fetchProducts = async () => {
  loadingProducts.value = true
  try {
    const data = await getMerchantProductList({ page: 1, pageSize: 100 })
    merchantProducts.value = data.records || []
  } finally {
    loadingProducts.value = false
  }
}

const openProductDialog = (row = null) => {
  productForm.value = row
    ? {
      id: row.id,
      name: row.name,
      categoryId: Number(row.categoryId || 1),
      price: Number(row.price || 0),
      originalPrice: Number(row.originalPrice || row.price || 0),
      stock: Number(row.stock || 0),
      mainImage: row.mainImage || '',
      description: row.description || ''
    }
    : {
      id: null,
      name: '',
      categoryId: 1,
      price: 10,
      originalPrice: 12,
      stock: 100,
      mainImage: '',
      description: ''
    }
  productVisible.value = true
}

const submitProduct = async () => {
  if (!productForm.value.name.trim()) {
    ElMessage.warning('请输入商品名称')
    return
  }
  productSaving.value = true
  try {
    await saveMerchantProduct(productForm.value)
    ElMessage.success('商品已保存')
    productVisible.value = false
    await fetchProducts()
    await fetchTraces()
  } finally {
    productSaving.value = false
  }
}

const toggleProduct = async (row, status) => {
  await updateMerchantProductStatus(row.id, { auditStatus: status })
  ElMessage.success(status === 1 ? '商品已上架' : '商品已下架')
  fetchProducts()
}

const fetchTraces = async () => {
  loadingTraces.value = true
  try {
    const data = await getMerchantTraceList({ page: 1, pageSize: 100 })
    merchantTraces.value = data.records || []
  } finally {
    loadingTraces.value = false
  }
}

const openTraceDialog = (row) => {
  traceForm.value = {
    productId: row.productId,
    originName: row.originName || '',
    originAddress: row.originAddress || '',
    plantMethod: row.plantMethod || '',
    plantTime: row.plantTime || '',
    harvestTime: row.harvestTime || '',
    storageCondition: row.storageCondition || '',
    shelfLife: Number(row.shelfLife || 5),
    testReport: row.testReport || ''
  }
  traceVisible.value = true
}

const submitTrace = async () => {
  traceSaving.value = true
  try {
    await saveMerchantTrace(traceForm.value.productId, traceForm.value)
    ElMessage.success('溯源信息已更新')
    traceVisible.value = false
    fetchTraces()
  } finally {
    traceSaving.value = false
  }
}

const showTraceQrcode = async (row) => {
  const data = await getMerchantTraceQrcode(row.productId)
  traceQrcode.value = {
    url: data.qrUrl,
    traceUrl: data.traceUrl
  }
  traceQrcodeVisible.value = true
}

const fetchMerchantOrders = async () => {
  loadingOrders.value = true
  try {
    const data = await getMerchantOrderList({ page: 1, pageSize: 50 })
    merchantOrders.value = data.records || []
  } finally {
    loadingOrders.value = false
  }
}

const fetchMerchantStats = async () => {
  loadingStats.value = true
  try {
    merchantStats.value = await getMerchantStats()
  } finally {
    loadingStats.value = false
  }
}

const openCreateDialog = () => {
  isEdit.value = false
  form.value = { id: null, title: '', content: '' }
  imageInput.value = ''
  productIdsInput.value = ''
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  form.value = { id: row.id, title: row.title, content: row.content }
  imageInput.value = (row.images || []).join(',')
  productIdsInput.value = (row.productIds || []).join(',')
  dialogVisible.value = true
}

const buildPayload = () => {
  const images = imageInput.value
    .split(',')
    .map(i => i.trim())
    .filter(Boolean)

  const productIds = productIdsInput.value
    .split(',')
    .map(i => Number(i.trim()))
    .filter(i => Number.isFinite(i) && i > 0)

  return {
    title: form.value.title.trim(),
    content: form.value.content.trim(),
    images,
    productIds
  }
}

const savePost = async () => {
  if (!form.value.title.trim()) {
    ElMessage.warning('请输入动态标题')
    return
  }
  if (!form.value.content.trim()) {
    ElMessage.warning('请输入动态内容')
    return
  }

  saving.value = true
  try {
    const payload = buildPayload()
    if (isEdit.value && form.value.id) {
      await updateMerchantCirclePost(form.value.id, payload)
      ElMessage.success('动态已更新')
    } else {
      await createMerchantCirclePost(payload)
      ElMessage.success('动态发布成功')
    }
    dialogVisible.value = false
    await fetchCircle()
  } finally {
    saving.value = false
  }
}

const removePost = async (row) => {
  await ElMessageBox.confirm(`确认删除动态“${row.title}”吗？`, '删除确认', { type: 'warning' })
  await deleteMerchantCirclePost(row.id)
  ElMessage.success('动态已删除')
  await fetchCircle()
}

const statusTextMap = {
  pending: '待支付',
  shipped: '待发货',
  delivered: '待收货',
  completed: '已完成',
  cancelled: '已取消',
  refunding: '售后处理中'
}

const openShipDialog = (row) => {
  shipForm.value.orderId = row.id
  shipForm.value.logisticsCompany = '郑州冷链快配'
  shipForm.value.logisticsNo = ''
  shipDialogVisible.value = true
}

const submitShip = async () => {
  if (!shipForm.value.logisticsNo.trim()) {
    ElMessage.warning('请输入物流单号')
    return
  }
  shipping.value = true
  try {
    await shipMerchantOrder(shipForm.value.orderId, {
      logisticsCompany: shipForm.value.logisticsCompany,
      logisticsNo: shipForm.value.logisticsNo.trim()
    })
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    await fetchMerchantOrders()
  } finally {
    shipping.value = false
  }
}

const fetchData = async () => {
  await merchantStore.fetchShopInfo()
  await fetchProducts()
  await fetchCircle()
  await fetchTraces()
  await fetchMerchantOrders()
  await fetchMerchantStats()
}

onMounted(fetchData)
</script>

<style scoped>
.shop-manage-container { min-height: 100vh; }
.shop-manage { min-height: 100vh; }
.shop-header {
  background: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid #dbe7df;
  padding: 0;
}
.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 62px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}
.header-content h2 { margin: 0; }
.shop-main { padding: 18px; }
.shop-info-card { max-width: 980px; margin: 0 auto; }
.shop-actions { margin-top: 18px; text-align: right; }
.pane-head { display: flex; align-items: center; justify-content: space-between; }
.pane-head h3 { margin: 0; }
.kpi-title { color: #5f7769; font-size: 14px; }
.kpi-value { margin-top: 8px; font-size: 28px; font-weight: 700; color: #274236; }
.qr-wrap { display: flex; flex-direction: column; align-items: center; gap: 8px; }
.qr-wrap p { margin: 0; color: #4f6c61; font-size: 13px; word-break: break-all; text-align: center; }
</style>
