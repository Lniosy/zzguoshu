import request from '@/utils/request'

export const getShopList = (params) => {
  return request({
    url: '/shop/list',
    method: 'get',
    params
  })
}

export const getShopDetail = (id) => {
  return request({
    url: `/shop/${id}`,
    method: 'get'
  })
}

export const getShopProducts = (shopId, params) => {
  return request({
    url: `/shop/${shopId}/products`,
    method: 'get',
    params
  })
}

export const searchShop = (keyword, params) => {
  return request({
    url: `/shop/search/${keyword}`,
    method: 'get',
    params
  })
}
