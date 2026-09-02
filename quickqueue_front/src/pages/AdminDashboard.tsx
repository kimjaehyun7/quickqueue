import React, { useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import { adminGetReservations, adminCall, adminComplete, adminCancel, adminCloseEvent, adminGetEvent, adminConnectSSE } from '../api/client'

type ReservationItem = {
  reservationToken: string
  waitingNumber: number
  representativeName: string
  peopleCount: number
  status: string
}

export default function AdminDashboard() {
  const { publicId } = useParams()
  const [list, setList] = useState<ReservationItem[]>([])
  const [eventStatus, setEventStatus] = useState('')
  const isClosed = /closed|end|ended|CLOSED/i.test(eventStatus || '')
  const sseRef = useRef<{ close: () => void } | null>(null)
  const refreshIntervalRef = useRef<number | null>(null)

  useEffect(() => {
    if (!publicId) return
    (async () => {
      try {
        const data = await adminGetReservations(publicId)
        setList(data || [])
      } catch (err) {
        // ignore
      }
      try {
        const ev = await adminGetEvent(publicId)
        setEventStatus(ev?.status || ev?.state || '')
      } catch (err) {
        // ignore
      }
      // finished loading
    })()
  }, [publicId])

  useEffect(() => {
    if (!publicId) return

    const handleSse = (evt: { type: string; data: string }) => {
      console.log('[AdminDashboard] received SSE event', {
        type: evt.type,
        rawData: evt.data
      })

      if (evt.type === 'connected' || evt.type === 'list') {
        console.log('[AdminDashboard] connected/list event received, reloading reservations via API')

        adminGetReservations(publicId)
          .then((data) => {
            console.log('[AdminDashboard] SSE reloaded reservations', data)
            setList(data || [])
          })
          .catch((err) => {
            console.error('[AdminDashboard] SSE reload reservations failed', err)
          })

        adminGetEvent(publicId)
          .then((ev) => {
            setEventStatus(ev?.status || ev?.state || '')
          })
          .catch((err) => {
            console.error('[AdminDashboard] SSE reload event status failed', err)
          })
      }
    }

    const sse = adminConnectSSE(publicId, handleSse)
    sseRef.current = sse

    refresh().catch(() => {})

    return () => {
      try { sseRef.current?.close() } catch {}
      if (refreshIntervalRef.current !== null) {
        window.clearInterval(refreshIntervalRef.current)
        refreshIntervalRef.current = null
      }
    }
  }, [publicId])

  const refresh = async () => {
    if (!publicId) return
    try {
      const data = await adminGetReservations(publicId)
      console.log('[AdminDashboard] refresh reservations result', data)
      setList(data || [])
    } catch (err) {
      console.error('[AdminDashboard] refresh reservations failed', err)
    }
    try {
      const ev = await adminGetEvent(publicId)
      console.log('[AdminDashboard] refresh event result', ev)
      setEventStatus(ev?.status || ev?.state || '')
    } catch (err) {
      console.error('[AdminDashboard] refresh event failed', err)
    }
  }

  const doCall = async (token: string) => { if (!publicId) return; await adminCall(publicId, token); refresh() }
  const doComplete = async (token: string) => { if (!publicId) return; await adminComplete(publicId, token); refresh() }
  const doCancel = async (token: string) => { if (!publicId) return; await adminCancel(publicId, token); refresh() }


  const openReservationPage = () => {
    if (!publicId) return
    const url = `${window.location.origin}/events/${publicId}`
    window.open(url, '_blank')
  }

  const copyReservationUrl = async () => {
    if (!publicId) return
    const url = `${window.location.origin}/events/${publicId}`
    try {
      await navigator.clipboard.writeText(url)
      alert('공용 예약 페이지 URL이 복사되었습니다.')
    } catch (err) {
      prompt('아래 URL을 복사하세요:', url)
    }
  }

  return (
    <div className="card">
      <h1>관리자 대시보드 - {publicId}</h1>
      <div style={{ marginBottom: 12 }}>
        <button style={{ marginLeft: 8 }} onClick={openReservationPage}>예약 페이지 열기</button>
        <button style={{ marginLeft: 8 }} onClick={copyReservationUrl}>URL 복사</button>
        <button style={{ marginLeft: 8 }} onClick={async () => {
          if (!publicId) return alert('publicId 필요')
          if (!confirm('이 이벤트를 종료하시겠습니까?')) return
          try {
            await adminCloseEvent(publicId)
            setEventStatus('CLOSED')
            await refresh()
            alert('이벤트를 종료했습니다.')
          } catch (err) {
            console.error(err)
            alert('이벤트 종료에 실패했습니다.')
          }
        }}>이벤트 종료</button>
      </div>

      {isClosed && <div className="muted-note">이 이벤트는 종료되었습니다. 예약 및 호출 작업이 비활성화됩니다.</div>}

      <table className="reservations">
        <thead>
          <tr><th>대기번호</th><th>대표자</th><th>인원</th><th>상태</th><th>작업</th></tr>
        </thead>
        <tbody>
          {list.map(r => (
            <tr key={r.reservationToken}>
              <td>{r.waitingNumber}</td>
              <td>{r.representativeName}</td>
              <td>{r.peopleCount}</td>
              <td>{r.status}</td>
              <td>
                <button disabled={isClosed} onClick={() => doCall(r.reservationToken)}>호출</button>
                <button disabled={isClosed} onClick={() => doComplete(r.reservationToken)} style={{ marginLeft: 6 }}>완료</button>
                <button disabled={isClosed} onClick={() => doCancel(r.reservationToken)} style={{ marginLeft: 6 }}>취소</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
