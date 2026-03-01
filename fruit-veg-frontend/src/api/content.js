import request from '@/utils/request'

export const getHomeBanners = () => request.get('/content/banners')
export const getNoticeList = () => request.get('/content/notices')

