import request from '@/utils/request'

// 订单管理
export const getOrderList = (params) => request.get('/order/list', { params })
export const getOrderDetail = (id) => request.get(`/order/${id}`)
export const createOrder = (data) => request.post('/order/create', data)
export const cancelOrder = (id) => request.put(`/order/${id}/cancel`)
export const confirmOrder = (id) => request.put(`/order/${id}/confirm`)
export const payOrder = (id, data) => request.put(`/order/${id}/pay`, data)
export const confirmReceive = (id) => request.put(`/order/${id}/receive`)
export const applyRefund = (id, data) => request.post(`/order/${id}/refund`, data)
export const getAfterSaleList = (params) => request.get('/order/after-sales', { params })
export const submitOrderReview = (id, data) => request.post(`/order/${id}/review`, data)
export const getOrderReview = (id) => request.get(`/order/${id}/review`)
