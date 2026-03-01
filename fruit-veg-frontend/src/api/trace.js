import request from '@/utils/request'

export const getTraceDetail = (productId) => request.get(`/trace/detail/${productId}`)
