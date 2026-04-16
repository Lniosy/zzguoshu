import request from '@/utils/request'

// 商家入驻
export const merchantApply = (data) => request.post('/merchant/apply', data)

// 店铺管理
export const getShopInfo = () => request.get('/merchant/info')
export const updateShopInfo = (data) => request.put('/merchant/info', data)

// 果蔬圈管理
export const getMerchantCircleList = (params) => request.get('/merchant/circle/list', { params })
export const createMerchantCirclePost = (data) => request.post('/merchant/circle', data)
export const updateMerchantCirclePost = (id, data) => request.put(`/merchant/circle/${id}`, data)
export const deleteMerchantCirclePost = (id) => request.delete(`/merchant/circle/${id}`)

// 订单履约
export const getMerchantOrderList = (params) => request.get('/merchant/orders', { params })
export const getMerchantOrderDetail = (id) => request.get(`/merchant/orders/${id}`)
export const shipMerchantOrder = (id, data) => request.put(`/merchant/orders/${id}/ship`, data)
export const getMerchantAfterSaleList = (params) => request.get('/merchant/after-sales', { params })
export const handleMerchantAfterSale = (id, data) => request.put(`/merchant/after-sales/${id}/handle`, data)

// 商家商品与溯源
export const getMerchantProductList = (params) => request.get('/merchant/products', { params })
export const saveMerchantProduct = (data) => request.post('/merchant/products', data)
export const updateMerchantProductStatus = (id, data) => request.put(`/merchant/products/${id}/status`, data)
export const getMerchantTraceList = (params) => request.get('/merchant/trace/list', { params })
export const saveMerchantTrace = (productId, data) => request.put(`/merchant/trace/${productId}`, data)
export const getMerchantTraceQrcode = (productId) => request.get(`/merchant/trace/${productId}/qrcode`)
export const getMerchantStats = () => request.get('/merchant/stats')

// 商家店铺对外展示
export const getPublicMerchantDetail = (id) => request.get(`/merchant/public/${id}`)
