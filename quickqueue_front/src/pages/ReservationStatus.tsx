import React, { useEffect, useState, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { getReservation, connectSSE } from '../api/client'

function normalizeReservationPayload(payload: any, currentToken?: string) {
  if (Array.isArray(payload)) {
    const matched = payload.find((item: any) => item?.reservationToken === currentToken)
    if (matched) {
      return matched
    }

    const index = payload.findIndex((item: any) => item?.reservationToken === currentToken)
    if (index >= 0) {
      const item = payload[index]
      return {
        waitingNumber: item?.waitingNumber,
        waitingAhead: index,
        peopleCount: item?.peopleCount,
        status: item?.status,
        reservationToken: item?.reservationToken
      }
    }

    return {}
  }

  if (payload && typeof payload === 'object') {
    if (payload.reservationToken === currentToken || payload.token === currentToken) {
      return payload
    }

    if (Array.isArray(payload.reservations)) {
      const queueItem = payload.reservations.find((item: any) => item?.reservationToken === currentToken)
      if (queueItem) {
        return {
          waitingNumber: queueItem.waitingNumber,
          waitingAhead: payload.reservations.findIndex((item: any) => item?.reservationToken === currentToken),
          peopleCount: queueItem.peopleCount,
          status: queueItem.status,
          reservationToken: queueItem.reservationToken
        }
      }
    }
  }

  return payload ?? {}
}

function attachSseHandlers(token: string, onPayload: (payload: any) => void) {
  const handle = (eventType: string, raw: string) => {
    console.log('[ReservationStatus] SSE event', { eventType, raw })
    try {
      const payload = raw === 'null' || raw === 'undefined' ? null : JSON.parse(raw)

      if (eventType === 'called' || eventType === 'waiting' || eventType === 'canceled' || eventType === 'completed') {
        onPayload(normalizeReservationPayload(payload, token))
        return
      }

      if (eventType === 'queue') {
        if (Array.isArray(payload)) {
          const current = payload.find((item: any) => item?.reservationToken === token)
          if (current) {
            onPayload({
              waitingNumber: current.waitingNumber,
              waitingAhead: payload.findIndex((item: any) => item?.reservationToken === token),
              peopleCount: current.peopleCount,
              status: current.status,
              reservationToken: current.reservationToken
            })
            return
          }
        }
      }

      if (payload && typeof payload === 'object') {
        onPayload(normalizeReservationPayload(payload, token))
      }
    } catch (err) {
      console.error('[ReservationStatus] SSE parse error', err)
    }
  }

  return (evt: { type: string; data: string }) => {
    handle(evt.type, evt.data)
  }
}

type Reservation = {
  waitingNumber?: number
  waitingAhead?: number
  peopleCount?: number
  status?: string
}

function getStatusMessage(status?: string) {
  switch ((status || '').toUpperCase()) {
    case 'WAITING':
      return '조금만 기다려주세요.'
    case 'CALLED':
      return '입장해주세요.'
    case 'COMPLETED':
      return '예약이 완료되었습니다.'
    case 'CANCELED':
      return '예약이 취소되었습니다.'
    default:
      return '예약 정보를 확인 중입니다.'
  }
}

export default function ReservationStatus() {
  const { token } = useParams()
  const [resv, setResv] = useState<Reservation>({})
  const sseRef = useRef<{ close: () => void } | null>(null)

  useEffect(() => {
    if (!token) return
    getReservation(token)
      .then(data => setResv({ waitingNumber: data.waitingNumber, waitingAhead: data.waitingAhead, peopleCount: data.peopleCount, status: data.status }))
      .catch(() => {})

    const handleSse = attachSseHandlers(token, (payload) => setResv(prev => ({ ...prev, ...payload })))
    const sse = connectSSE(token, handleSse)
    sseRef.current = sse

    return () => {
      try { sseRef.current?.close() } catch {}
    }
  }, [token])

  return (
    <div className="card centered reservation-status-card">
      <h1>예약 현황</h1>
      <div className="status-box" aria-live="polite">
        <div className="status-number-label">내 대기번호</div>
        <div className="big">{resv.waitingNumber ?? '-'}</div>
        <div className="highlight">내 앞: {resv.waitingAhead ?? '-'}팀</div>
        <div className="muted-note status-message">{getStatusMessage(resv.status)}</div>
      </div>
    </div>
  )
}
