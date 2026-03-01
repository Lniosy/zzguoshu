import request from '@/utils/request'

export const getFavoriteList = () => request.get('/favorite/list')
export const addFavorite = (productId) => request.post(`/favorite/${productId}`)
export const removeFavorite = (productId) => request.delete(`/favorite/${productId}`)
