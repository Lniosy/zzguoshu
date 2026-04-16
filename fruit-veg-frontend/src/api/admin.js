import request from '@/utils/request'

// 系统管理员接口

// 获取用户列表
export const getUserList = (params) => request.get('/admin/users', { params })

// 获取用户详情
export const getUserDetail = (id) => request.get(`/admin/users/${id}`)

// 禁用/启用用户
export const toggleUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, { status })
export const exportUsers = (params) => request.get('/admin/users/export', { params })
export const createSubAdmin = (data) => request.post('/admin/sub-admins', data)

// 获取商品列表
export const getProductList = (params) => request.get('/admin/products', { params })

// 获取商品详情
export const getProductDetail = (id) => request.get(`/admin/products/${id}`)

// 审核商品
export const auditProduct = (id, status) => request.put(`/admin/products/${id}/audit`, { status })

// 获取订单列表
export const getOrderList = (params) => request.get('/admin/orders', { params })

// 获取订单详情
export const getOrderDetail = (id) => request.get(`/admin/orders/${id}`)

// 发货
export const shipOrder = (id, data) => request.put(`/admin/orders/${id}/ship`, data)

// 获取商家列表
export const getMerchantList = (params) => request.get('/admin/merchants', { params })

// 获取商家详情
export const getMerchantDetail = (id) => request.get(`/admin/merchants/${id}`)

// 审核商家申请
export const auditMerchant = (id, status) => request.put(`/admin/merchants/${id}/audit`, { status })

// 获取系统统计数据
export const getSystemStats = () => request.get('/admin/stats')

// 投诉/售后处理
export const getComplaintList = (params) => request.get('/admin/complaints', { params })
export const handleComplaint = (id, data) => request.put(`/admin/complaints/${id}/handle`, data)

// 内容运营
export const getAdminBanners = (params) => request.get('/admin/content/banners', { params })
export const saveAdminBanner = (data) => request.post('/admin/content/banner', data)
export const deleteAdminBanner = (id) => request.delete(`/admin/content/banner/${id}`)
export const getAdminNotices = (params) => request.get('/admin/content/notices', { params })
export const saveAdminNotice = (data) => request.post('/admin/content/notice', data)
export const deleteAdminNotice = (id) => request.delete(`/admin/content/notice/${id}`)
export const getAdminCircleCommentList = (params) => request.get('/admin/circle/comments', { params })
export const auditAdminCircleComment = (postId, commentId, data) => request.put(`/admin/circle/comments/${postId}/${commentId}/audit`, data)

// 系统参数设置
export const getSystemSettings = () => request.get('/admin/settings')
export const updateSystemSettings = (data) => request.put('/admin/settings', data)
export const getRoleList = () => request.get('/admin/roles')
export const updateRolePermissions = (role, permissions) => request.put(`/admin/roles/${role}/permissions`, { permissions })

// 溯源管理
export const getTraceTemplateList = () => request.get('/admin/traces/templates')
export const saveTraceTemplate = (data) => request.post('/admin/traces/template', data)
export const deleteTraceTemplate = (id) => request.delete(`/admin/traces/template/${id}`)
export const getAdminTraceList = (params) => request.get('/admin/traces', { params })
export const auditAdminTrace = (productId, data) => request.put(`/admin/traces/${productId}/audit`, data)
export const getAdminTraceStats = () => request.get('/admin/traces/stats')
