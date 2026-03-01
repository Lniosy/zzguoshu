import request from '@/utils/request'

// 商品分类
export const getCategoryList = () => request.get('/category/list')
export const getCategoryTree = () => request.get('/category/tree')

// 商品管理
export const getProductList = (params) => request.get('/product/list', { params })
export const getProductDetail = (id) => request.get(`/product/${id}`)
export const searchProduct = (keyword) => request.get('/product/search', { params: { keyword } })
export const getRecommendProducts = () => request.get('/product/recommend')
