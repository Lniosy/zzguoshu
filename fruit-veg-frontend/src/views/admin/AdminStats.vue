<template>
  <div class="admin-stats">
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">总用户数</p>
              <h3 class="stat-value">{{ stats.userCount }}</h3>
            </div>
            <div class="stat-icon"><el-icon class="icon"><User /></el-icon></div>
          </div>
          <el-progress :percentage="userGrowth" :show-text="false" :stroke-width="6" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">总商品数</p>
              <h3 class="stat-value">{{ stats.productCount }}</h3>
            </div>
            <div class="stat-icon"><el-icon class="icon"><Goods /></el-icon></div>
          </div>
          <el-progress :percentage="productGrowth" :show-text="false" :stroke-width="6" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">总订单数</p>
              <h3 class="stat-value">{{ stats.orderCount }}</h3>
            </div>
            <div class="stat-icon"><el-icon class="icon"><Ticket /></el-icon></div>
          </div>
          <el-progress :percentage="orderGrowth" :show-text="false" :stroke-width="6" />
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">总销售额</p>
              <h3 class="stat-value">¥{{ (stats.totalAmount / 100).toFixed(2) }}</h3>
            </div>
            <div class="stat-icon"><el-icon class="icon"><Money /></el-icon></div>
          </div>
          <el-progress :percentage="amountGrowth" :show-text="false" :stroke-width="6" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <h3>销售趋势</h3>
            <el-select v-model="timeRange" style="width: 120px">
              <el-option label="近7天" :value="7" />
              <el-option label="近30天" :value="30" />
              <el-option label="近90天" :value="90" />
              <el-option label="近1年" :value="365" />
            </el-select>
          </div>
          <div ref="salesChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header"><h3>商品分类分布</h3></div>
          <div ref="categoryChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header"><h3>投诉处理概览</h3></div>
          <div class="complaint-overview">
            <div class="c-item">
              <div class="label">待处理投诉</div>
              <div class="value warning">{{ stats.complaintPendingCount || 0 }}</div>
            </div>
            <div class="c-item">
              <div class="label">已处理投诉</div>
              <div class="value success">{{ stats.complaintHandledCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header"><h3>订单状态分布</h3></div>
          <div ref="orderStatusChartRef" class="chart-container" />
        </el-card>
      </el-col>
      <el-col :span="12" style="margin-top: 16px">
        <el-card class="chart-card">
          <div class="chart-header"><h3>支付方式分布</h3></div>
          <div ref="paymentChartRef" class="chart-container" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue'
import { getSystemStats } from '@/api/admin'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts/core'
import { LineChart, PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, PieChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const stats = reactive({
  userCount: 0,
  productCount: 0,
  orderCount: 0,
  totalAmount: 0,
  complaintPendingCount: 0,
  complaintHandledCount: 0
})
const timeRange = ref(30)

const userGrowth = ref(25)
const productGrowth = ref(15)
const orderGrowth = ref(30)
const amountGrowth = ref(20)

const salesChartRef = ref()
const categoryChartRef = ref()
const orderStatusChartRef = ref()
const paymentChartRef = ref()

const salesChartOption = {
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: ['1', '2', '3', '4', '5', '6', '7'] },
  yAxis: { type: 'value' },
  series: [{ name: '销售额', type: 'line', data: [120, 132, 101, 134, 90, 230, 210], smooth: true }]
}

const categoryChartOption = {
  tooltip: { trigger: 'item' },
  legend: { top: '5%', left: 'center' },
  series: [{ type: 'pie', radius: ['40%', '70%'], data: [
    { value: 1048, name: '水果' }, { value: 735, name: '蔬菜' }, { value: 580, name: '坚果' }, { value: 484, name: '肉类' }, { value: 300, name: '海鲜' }
  ] }]
}

const orderStatusChartOption = {
  tooltip: { trigger: 'item' },
  legend: { top: '5%', left: 'center' },
  series: [{ type: 'pie', radius: ['40%', '70%'], data: [
    { value: 335, name: '待付款' }, { value: 310, name: '待发货' }, { value: 234, name: '待收货' }, { value: 135, name: '待评价' }, { value: 148, name: '已完成' }, { value: 123, name: '已取消' }
  ] }]
}

const paymentChartOption = {
  tooltip: { trigger: 'item' },
  legend: { top: '5%', left: 'center' },
  series: [{ type: 'pie', radius: ['40%', '70%'], data: [
    { value: 448, name: '微信支付' }, { value: 335, name: '支付宝' }, { value: 310, name: '银行卡' }, { value: 234, name: '货到付款' }, { value: 135, name: '其他' }
  ] }]
}

const fetchStats = async () => {
  try {
    Object.assign(stats, await getSystemStats())
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

const initCharts = () => {
  if (salesChartRef.value) echarts.init(salesChartRef.value).setOption(salesChartOption)
  if (categoryChartRef.value) echarts.init(categoryChartRef.value).setOption(categoryChartOption)
  if (orderStatusChartRef.value) echarts.init(orderStatusChartRef.value).setOption(orderStatusChartOption)
  if (paymentChartRef.value) echarts.init(paymentChartRef.value).setOption(paymentChartOption)
}

onMounted(async () => {
  await fetchStats()
  await nextTick()
  initCharts()
})
</script>

<style scoped>
.admin-stats { padding: 0; }
.stat-card { height: 100%; }
.stat-content { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.stat-label { margin: 0; font-size: 14px; color: #606266; }
.stat-value { margin: 5px 0 0; font-size: 24px; color: #303133; font-weight: bold; }
.stat-icon { display: flex; align-items: center; justify-content: center; width: 60px; height: 60px; border-radius: 12px; background-color: #f0f9ff; }
.stat-icon .icon { font-size: 32px; color: #409eff; }
.chart-card { height: 100%; }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.chart-header h3 { margin: 0; font-size: 18px; color: #303133; }
.chart-container { height: 300px; }
.complaint-overview { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; padding-top: 10px; }
.c-item { background: #f6fbf8; border: 1px solid #dce9e2; border-radius: 10px; padding: 14px; }
.c-item .label { color: #567266; font-size: 14px; }
.c-item .value { margin-top: 6px; font-size: 30px; font-weight: 700; }
.c-item .value.warning { color: #d18f11; }
.c-item .value.success { color: #1f8f5f; }
</style>
