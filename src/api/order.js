import request from '@/utils/request'

export const createOrder = (data) => {
  return request({
    url: '/order/create',
    method: 'post',
    data
  })
}

export const getOrderList = (params) => {
  return request({
    url: '/order/list',
    method: 'get',
    params
  })
}

export const getOrderDetail = (id) => {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

export const updateOrderStatus = (id, status) => {
  return request({
    url: `/order/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export const cancelOrder = (id) => {
  return request({
    url: `/order/${id}/cancel`,
    method: 'put'
  })
}
