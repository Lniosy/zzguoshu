<template>
  <div class="checkout-page">
    <div class="page-header">
      <h2>订单结算</h2>
      <el-button link @click="navigate('/cart')">返回购物车</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header><h3>收货地址</h3></template>
          <el-alert
            v-if="addresses.length === 0"
            title="暂无收货地址，请先新增地址后再结算"
            type="warning"
            :closable="false"
            style="margin-bottom: 10px"
          />
          <el-button text type="primary" @click="navigate('/user/address')" style="margin-bottom: 8px">去管理地址</el-button>
          <el-radio-group v-model="selectedAddressId" class="address-list">
            <el-radio v-for="addr in addresses" :key="addr.id" :value="addr.id">
              {{ addr.name }} / {{ addr.phone }} / {{ addr.fullAddress }}
            </el-radio>
          </el-radio-group>
        </el-card>

        <el-card class="mt-12">
          <template #header><h3>商品清单</h3></template>
          <el-table :data="items">
            <el-table-column prop="name" label="商品" />
            <el-table-column prop="price" label="单价">
              <template #default="{ row }">¥{{ Number(row.price).toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" />
            <el-table-column label="小计">
              <template #default="{ row }">¥{{ (Number(row.price) * Number(row.quantity)).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header><h3>支付信息</h3></template>
          <el-form label-width="90px">
            <el-form-item label="支付方式">
              <el-select v-model="paymentMethod" style="width: 100%">
                <el-option label="微信支付" value="微信支付" />
                <el-option label="支付宝" value="支付宝" />
              </el-select>
            </el-form-item>
          </el-form>
          <div class="total-box">
            <p>商品总额：<b>¥{{ total.toFixed(2) }}</b></p>
            <p>配送费：<b>¥0.00</b></p>
            <p class="pay">应付金额：<span>¥{{ total.toFixed(2) }}</span></p>
          </div>
          <el-button type="primary" style="width: 100%" @click="submitOrder" :loading="submitting">提交订单并支付</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAppNavigation } from '@/composables/useAppNavigation'
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { createOrder, payOrder } from '@/api/order'

const router = useRouter()
const { navigate } = useAppNavigation()
const cartStore = useCartStore()
const userStore = useUserStore()

const selectedAddressId = ref(null)
const paymentMethod = ref('微信支付')
const submitting = ref(false)

const items = computed(() => cartStore.getCartItems)
const addresses = computed(() => userStore.getAddressList)
const total = computed(() => items.value.reduce((sum, i) => sum + Number(i.price) * Number(i.quantity), 0))

const selectedAddress = computed(() => addresses.value.find(a => a.id === selectedAddressId.value))

const submitOrder = async () => {
  if (!selectedAddress.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (items.value.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  const merchantIds = new Set(items.value.map(item => Number(item.merchantId || 0)).filter(id => id > 0))
  if (merchantIds.size > 1) {
    ElMessage.warning('一次下单仅支持同一商家商品，请按商家分开下单')
    return
  }

  submitting.value = true
  try {
    const order = await createOrder({
      totalAmount: total.value,
      paymentMethod: paymentMethod.value,
      receiverName: selectedAddress.value.name,
      receiverPhone: selectedAddress.value.phone,
      receiverAddress: selectedAddress.value.fullAddress,
      items: items.value
    })
    await payOrder(order.id, { paymentMethod: paymentMethod.value })
    items.value.forEach(i => cartStore.removeFromCart(i.id))
    ElMessage.success('下单并支付成功')
    navigate(`/orders/${order.id}`)
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await userStore.fetchAddressList()
  const def = addresses.value.find(a => a.isDefault)
  selectedAddressId.value = def?.id || addresses.value[0]?.id || null
})
</script>

<style scoped>
.checkout-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.address-list { display: flex; flex-direction: column; gap: 8px; }
.mt-12 { margin-top: 12px; }
.total-box { margin: 14px 0; padding: 10px; border-radius: 10px; background: #f6fbf8; }
.total-box .pay span { color: #0f8b5f; font-size: 24px; font-weight: 700; }
</style>
