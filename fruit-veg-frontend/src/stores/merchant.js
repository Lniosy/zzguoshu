import { defineStore } from 'pinia'
import { merchantApply, getShopInfo, updateShopInfo } from '@/api/merchant'
import { ElMessage } from 'element-plus'

export const useMerchantStore = defineStore('merchant', {
  state: () => ({
    shopInfo: null,
    isMerchant: false
  }),
  getters: {
    getShopInfo: (state) => state.shopInfo,
    getIsMerchant: (state) => state.isMerchant
  },
  actions: {
    // 商家入驻申请
    async merchantApply(data) {
      try {
        const response = await merchantApply(data)
        ElMessage.success('入驻申请提交成功')
        this.isMerchant = true
        this.shopInfo = response
        return response
      } catch (error) {
        ElMessage.error(error.message || '入驻申请提交失败')
        throw error
      }
    },
    // 获取店铺信息
    async fetchShopInfo() {
      try {
        const response = await getShopInfo()
        this.shopInfo = response
        this.isMerchant = true
        return response
      } catch (error) {
        ElMessage.error(error.message || '获取店铺信息失败')
        this.isMerchant = false
        throw error
      }
    },
    // 更新店铺信息
    async updateShopInfo(data) {
      try {
        const response = await updateShopInfo(data)
        this.shopInfo = response
        ElMessage.success('店铺信息更新成功')
        return response
      } catch (error) {
        ElMessage.error(error.message || '店铺信息更新失败')
        throw error
      }
    }
  }
})
