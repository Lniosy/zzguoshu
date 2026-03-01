import { defineStore } from 'pinia'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: []
  }),
  getters: {
    getOrders: (state) => state.orders,
    getOrderCount: (state) => state.orders.length
  },
  actions: {
    async fetchOrders() {
      try {
        const { getOrderList } = await import('@/api/order')
        const data = await getOrderList()
        this.orders = data.records || []
        return this.orders
      } catch (error) {
        console.error('获取订单列表失败:', error)
        return []
      }
    },
    async fetchOrderDetail(id) {
      try {
        const { getOrderDetail } = await import('@/api/order')
        return await getOrderDetail(id)
      } catch (error) {
        console.error('获取订单详情失败:', error)
        return null
      }
    },
    async createOrder(data) {
      try {
        const { createOrder } = await import('@/api/order')
        return await createOrder(data)
      } catch (error) {
        console.error('创建订单失败:', error)
        return null
      }
    },
    async cancelOrder(id) {
      try {
        const { cancelOrder } = await import('@/api/order')
        await cancelOrder(id)
        this.orders = this.orders.filter(order => order.id !== id)
        return true
      } catch (error) {
        console.error('取消订单失败:', error)
        return false
      }
    },
    async confirmOrder(id) {
      try {
        const { confirmOrder } = await import('@/api/order')
        await confirmOrder(id)
        const order = this.orders.find(order => order.id === id)
        if (order) {
          order.status = 'confirmed'
        }
        return true
      } catch (error) {
        console.error('确认订单失败:', error)
        return false
      }
    },
    async payOrder(id, data) {
      try {
        const { payOrder } = await import('@/api/order')
        await payOrder(id, data)
        const order = this.orders.find(order => order.id === id)
        if (order) {
          order.status = 'paid'
          order.paymentTime = new Date()
        }
        return true
      } catch (error) {
        console.error('支付订单失败:', error)
        return false
      }
    },
    async confirmReceive(id) {
      try {
        const { confirmReceive } = await import('@/api/order')
        await confirmReceive(id)
        const order = this.orders.find(order => order.id === id)
        if (order) {
          order.status = 'completed'
          order.receiveTime = new Date()
        }
        return true
      } catch (error) {
        console.error('确认收货失败:', error)
        return false
      }
    },
    async applyRefund(id, data) {
      try {
        const { applyRefund } = await import('@/api/order')
        await applyRefund(id, data)
        const order = this.orders.find(order => order.id === id)
        if (order) {
          order.status = 'refund'
        }
        return true
      } catch (error) {
        console.error('申请退款失败:', error)
        return false
      }
    }
  }
})
