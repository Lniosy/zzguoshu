import request from '@/utils/request'

export const askAi = (question, sessionId) => request.post('/ai/ask', { question, sessionId }, { timeout: 40000 })
export const getAiHistory = (sessionId) => request.get('/ai/history', { params: sessionId ? { sessionId } : {} })
export const getAiFaq = () => request.get('/ai/faq')
export const getAiSessions = () => request.get('/ai/sessions')
export const createAiSession = () => request.post('/ai/sessions')
export const switchAiSession = (id) => request.put(`/ai/sessions/${id}/active`)
export const deleteAiSession = (id) => request.delete(`/ai/sessions/${id}`)
