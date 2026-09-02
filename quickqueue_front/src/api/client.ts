import axios from 'axios'
import { fetchEventSource } from '@microsoft/fetch-event-source'
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

export const connectSSE = (reservationToken: string, onEvent?: (event: { type: string; data: string }) => void) => {
  const url = `${API_BASE}/api/reservations/${reservationToken}/sse/connect`
  console.log('[SSE] user connect start', { reservationToken, url })

  const controller = new AbortController()

  fetchEventSource(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'text/event-stream',
      Accept: 'text/event-stream'
    },
    credentials: 'include',
    signal: controller.signal,
    onopen: async () => {
      console.log('[SSE] user stream opened', { reservationToken })
    },
    onmessage: (event) => {
      console.log('[SSE] user event received', { reservationToken, eventName: event.event ?? 'message', data: event.data })
      onEvent?.({ type: event.event ?? 'message', data: event.data })
    },
    onerror: (err) => {
      console.error('[SSE] user stream error', { reservationToken, err })
      throw err
    }
  })

  return {
    close: () => controller.abort(),
    abort: () => controller.abort()
  }
}

export const adminConnectSSE = (publicId: string, onEvent?: (event: { type: string; data: string }) => void) => {
  const url = `${API_BASE}/api/reservations/${publicId}/admin/sse/connect`
  console.log('[SSE] admin connect start', { publicId, url })

  const token = getAccessToken()
  const controller = new AbortController()

  fetchEventSource(url, {
    method: 'GET',
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
      'Content-Type': 'text/event-stream',
      Accept: 'text/event-stream'
    },
    credentials: 'include',
    signal: controller.signal,
    onopen: async () => {
      console.log('[SSE] admin stream opened', { publicId })
    },
    onmessage: (event) => {
      const eventType = (event as any)?.event || 'message'
      console.log('[SSE] admin event received', { publicId, eventName: eventType, data: event.data })
      onEvent?.({ type: eventType, data: event.data })
    },
    onerror: (err) => {
      console.error('[SSE] admin stream error', { publicId, err })
      throw err
    }
  })

  return {
    close: () => controller.abort(),
    abort: () => controller.abort()
  }
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
