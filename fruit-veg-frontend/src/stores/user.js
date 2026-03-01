import { defineStore } from 'pinia'
import { login, register, getUserInfo, updateUserInfo, getAddressList, addAddress, updateAddress, deleteAddress } from '@/api/user'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: localStorage.getItem('userInfo') ? JSON.parse(localStorage.getItem('userInfo')) : null,
    token: localStorage.getItem('token') || '',
    isLoggedIn: !!localStorage.getItem('token'),
    addressList: [],
    role: localStorage.getItem('role') || 'USER' // 默认用户角色
  }),
  getters: {
    getUserInfo: (state) => state.userInfo,
    getToken: (state) => state.token,
    getIsLoggedIn: (state) => state.isLoggedIn,
    getAddressList: (state) => state.addressList,
    getDefaultAddress: (state) => state.addressList.find(address => address.isDefault),
    getRole: (state) => state.role,
    isAdmin: (state) => state.role === 'ADMIN',
    isMerchant: (state) => state.role === 'MERCHANT',
    isUser: (state) => state.role === 'USER'
  },
  actions: {
    // 登录
    async login(data) {
      try {
        const response = await login(data)
        this.token = response.token
        this.isLoggedIn = true
        this.userInfo = response.userInfo
        this.role = response.userInfo.role || 'USER'
        localStorage.setItem('token', response.token)
        localStorage.setItem('role', this.role)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        ElMessage.success('登录成功')
        return response
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
        throw error
      }
    },
    // 注册
    async register(data) {
      try {
        const response = await register(data)
        ElMessage.success('注册成功')
        return response
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
        throw error
      }
    },
    // 获取用户信息
    async fetchUserInfo() {
      try {
        const response = await getUserInfo()
        this.userInfo = response
        this.role = response.role || 'USER'
        localStorage.setItem('role', this.role)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return response
      } catch (error) {
        ElMessage.error(error.message || '获取用户信息失败')
        throw error
      }
    },
    // 更新用户信息
    async updateUserInfo(data) {
      try {
        const response = await updateUserInfo(data)
        this.userInfo = { ...this.userInfo, ...data }
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        ElMessage.success('用户信息更新成功')
        return response
      } catch (error) {
        ElMessage.error(error.message || '用户信息更新失败')
        throw error
      }
    },
    // 获取地址列表
    async fetchAddressList() {
      try {
        const response = await getAddressList()
        this.addressList = (response || []).map(item => ({
          id: item.id,
          userId: item.userId,
          name: item.receiverName,
          phone: item.receiverPhone,
          fullAddress: item.receiverAddress,
          detail: item.receiverAddress,
          isDefault: item.isDefault === 1,
          createTime: item.createTime,
          updateTime: item.updateTime
        }))
        return this.addressList
      } catch (error) {
        ElMessage.error(error.message || '获取地址列表失败')
        throw error
      }
    },
    // 添加地址
    async addAddress(data) {
      try {
        const payload = {
          receiverName: data.name,
          receiverPhone: data.phone,
          receiverAddress: data.detail || data.fullAddress || '',
          isDefault: data.isDefault ? 1 : 0
        }
        const response = await addAddress(payload)
        await this.fetchAddressList()
        ElMessage.success('地址添加成功')
        return response
      } catch (error) {
        ElMessage.error(error.message || '地址添加失败')
        throw error
      }
    },
    // 更新地址
    async updateAddress(id, data) {
      try {
        const payload = {
          receiverName: data.name,
          receiverPhone: data.phone,
          receiverAddress: data.detail || data.fullAddress || '',
          isDefault: data.isDefault ? 1 : 0
        }
        const response = await updateAddress(id, payload)
        await this.fetchAddressList()
        ElMessage.success('地址更新成功')
        return response
      } catch (error) {
        ElMessage.error(error.message || '地址更新失败')
        throw error
      }
    },
    // 删除地址
    async deleteAddress(id) {
      try {
        await deleteAddress(id)
        this.addressList = this.addressList.filter(address => address.id !== id)
        ElMessage.success('地址删除成功')
      } catch (error) {
        ElMessage.error(error.message || '地址删除失败')
        throw error
      }
    },
    // 设置默认地址
    async setDefaultAddress(id) {
      try {
        const address = this.addressList.find(address => address.id === id)
        if (address) {
          await this.updateAddress(id, { ...address, isDefault: true })
          // 将其他地址的默认地址设置为 false
          this.addressList = this.addressList.map(item => {
            if (item.id !== id) {
              return { ...item, isDefault: false }
            }
            return item
          })
        }
      } catch (error) {
        ElMessage.error(error.message || '设置默认地址失败')
        throw error
      }
    },
    // 登出
    logout() {
      this.userInfo = null
      this.token = ''
      this.isLoggedIn = false
      this.addressList = []
      this.role = 'USER'
      localStorage.removeItem('token')
      localStorage.removeItem('role')
      localStorage.removeItem('userInfo')
      ElMessage.success('已退出登录')
    }
  }
})
