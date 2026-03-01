import request from '@/utils/request'

export const getProductList = (params) => {
  return request({
    url: '/product/list',
    method: 'get',
    params
  })
}

export const getProductDetail = (id) => {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

export const getProductByCategory = (categoryId, params) => {
  return request({
    url: `/product/category/${categoryId}`,
    method: 'get',
    params
  })
}

export const searchProduct = (keyword, params) => {
  return request({
    url: `/product/search/${keyword}`,
    method: 'get',
    params
  })
}
