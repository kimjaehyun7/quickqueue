import React, { useEffect, useState, useRef } from 'react'
import { useParams } from 'react-router-dom'
import { getReservation, connectSSE } from '../api/client'

function attachSseHandlers(es: EventSource, onPayload: (payload: any) => void) {
  const onMessage = (e: MessageEvent) => {
    try { onPayload(JSON.parse(e.data)) } catch (err) { console.error(err) }
  }
  const onWaiting = (e: MessageEvent) => { try { onPayload(JSON.parse(e.data)) } catch (err) {} }
  const onCalled = (e: MessageEvent) => { try { onPayload(JSON.parse(e.data)) } catch (err) {} }
  const onQueue = (e: MessageEvent) => { try { onPayload(JSON.parse(e.data)) } catch (err) {} }
  const onCanceled = (e: MessageEvent) => { try { onPayload(JSON.parse(e.data)) } catch (err) {} }
  const onCompleted = (e: MessageEvent) => { try { onPayload(JSON.parse(e.data)) } catch (err) {} }

  es.addEventListener('message', onMessage as any)
  es.addEventListener('waiting', onWaiting as any)
  es.addEventListener('called', onCalled as any)
  es.addEventListener('queue-updated', onQueue as any)
  es.addEventListener('canceled', onCanceled as any)
  es.addEventListener('completed', onCompleted as any)

  return () => {
    es.removeEventListener('message', onMessage as any)
    es.removeEventListener('waiting', onWaiting as any)
    es.removeEventListener('called', onCalled as any)
    es.removeEventListener('queue-updated', onQueue as any)
    es.removeEventListener('canceled', onCanceled as any)
    es.removeEventListener('completed', onCompleted as any)
  }
}

type Reservation = {
  waitingNumber?: number
  waitingAhead?: number
  peopleCount?: number
  status?: string
}

export default function ReservationStatus() {
  const { token } = useParams()
  const [resv, setResv] = useState<Reservation>({})
  const eventSourceRef = useRef<EventSource | null>(null)

  useEffect(() => {
    if (!token) return
    getReservation(token)
      .then(data => setResv({ waitingNumber: data.waitingNumber, waitingAhead: data.waitingAhead, peopleCount: data.peopleCount, status: data.status }))
      .catch(() => {})
    let es: EventSource | null = null
    let cleanup: (() => void) | null = null
    let backoff = 1000
    let stopped = false

    const connect = () => {
      es = connectSSE(token)
      eventSourceRef.current = es
      cleanup = attachSseHandlers(es, (payload) => setResv(prev => ({ ...prev, ...payload })))
      es.onerror = () => {
        if (stopped) return
        try { es?.close() } catch {};
        setTimeout(() => {
          backoff = Math.min(16000, backoff * 2)
          connect()
        }, backoff)
      }
    }

    connect()

    return () => {
      stopped = true
      try { cleanup && cleanup() } catch {}
      try { eventSourceRef.current?.close() } catch {}
    }
  }, [token])

  return (
    <div className="card centered">
      <h1>예약 현황</h1>
      <div className="status-box">
        <div className="big">대기번호 {resv.waitingNumber ?? '-'}</div>
        <div className="highlight">내 앞: {resv.waitingAhead ?? '-' }명</div>
        <div>인원수: {resv.peopleCount ?? '-'}</div>
        <div>상태: {resv.status ?? '-'}</div>
      </div>
    </div>
  )
}
