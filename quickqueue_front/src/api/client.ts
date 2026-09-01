import axios from 'axios'
import { getAccessToken } from '../auth'

const API_BASE = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080'

export const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
})

// Attach Authorization header if token exists in localStorage (for integration/testing)
api.interceptors.request.use((config) => {
  const token = getAccessToken()
  if (token) {
    config.headers = config.headers || {}
    ;(config.headers as any)['Authorization'] = `Bearer ${token}`
  }
  return config
})

export const createReservation = (publicId: string, body: any) =>
  api.post(`/api/reservations/${publicId}`, body).then(r => r.data)

export const getReservation = (reservationToken: string) =>
  api.get(`/api/reservations/${reservationToken}`).then(r => r.data)

export const connectSSE = (reservationToken: string) => {
  const url = `${API_BASE}/api/reservations/${reservationToken}/sse/connect`
  return new EventSource(url, { withCredentials: true } as any)
}

export const adminCreateEvent = (body: any) => api.post('/api/admin/events', body).then(r => r.data)
export const adminGetReservations = (publicId: string) =>
  api.get(`/api/admin/reservations/${publicId}`).then(r => r.data)
export const adminGetEvents = () => api.get('/api/admin/events').then(r => r.data)
export const adminGetEvent = (publicId: string) => api.get(`/api/admin/events/${publicId}`).then(r => r.data)
export const adminCloseEvent = (publicId: string) => api.post(`/api/admin/events/${publicId}/close`).then(r => r.data)
export const adminCall = (publicId: string, reservationToken: string) =>
  api.post(`/api/admin/reservations/${publicId}/${reservationToken}/call`)
export const adminComplete = (publicId: string, reservationToken: string) =>
  api.post(`/api/admin/reservations/${publicId}/${reservationToken}/complete`)
export const adminCancel = (publicId: string, reservationToken: string) =>
  api.post(`/api/admin/reservations/${publicId}/${reservationToken}/cancel`)

export default api
