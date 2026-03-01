import request from '@/utils/request'

export const getCircleList = (params) => request.get('/circle/list', { params })
export const getCircleDetail = (id) => request.get(`/circle/detail/${id}`)
export const likeCircle = (id) => request.post(`/circle/${id}/like`)
export const commentCircle = (id, data) => request.post(`/circle/${id}/comment`, data)
export const followMerchant = (merchantId) => request.post(`/circle/follow/${merchantId}`)
export const unfollowMerchant = (merchantId) => request.delete(`/circle/follow/${merchantId}`)
export const getFollowedMerchants = () => request.get('/circle/follow/merchants')
