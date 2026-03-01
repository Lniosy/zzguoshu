import request from '@/utils/request'

// 用户认证
export const register = (data) => request.post('/auth/register', data)
export const login = (data) => request.post('/auth/login', data)
export const loginByCode = (data) => request.post('/auth/login/code', data)
export const getLoginCode = (phone) => request.get('/auth/code', { params: { phone } })

// 用户信息
export const getUserInfo = () => request.get('/user/info')
export const updateUserInfo = (data) => request.put('/user/info', data)
export const updateUserPassword = (data) => request.put('/user/password', data)

// 收货地址
export const getAddressList = () => request.get('/user/address')
export const addAddress = (data) => request.post('/user/address', data)
export const updateAddress = (id, data) => request.put(`/user/address/${id}`, data)
export const deleteAddress = (id) => request.delete(`/user/address/${id}`)
