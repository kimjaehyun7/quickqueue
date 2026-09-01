import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminGetEvents } from '../api/client'

export default function AdminHome(){
  const [events, setEvents] = useState<any[]>([])
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    fetchEvents()
  }, [])

  const fetchEvents = async () => {
    setLoading(true)
    try {
      const res = await adminGetEvents()
      setEvents(res || [])
    } catch (err) {
      console.error(err)
      alert('이벤트 목록을 불러오지 못했습니다. 관리자 토큰이 필요할 수 있습니다.')
    } finally {
      setLoading(false)
    }
  }

  const openDashboard = (publicId: string) => {
    navigate(`/admin/dashboard/${publicId}`)
  }

  // backend cookie or real token flow expected

  return (
    <div className="centered">
      <div className="card" style={{ display: 'flex', flexDirection: 'column', gap: 12, width: '100%' }}>
        <h2>이벤트 목록</h2>
        
        <div>
          {events.length === 0 ? <p>이벤트가 없습니다.</p> : (
            <table className="reservations">
              <thead><tr><th>이벤트명</th><th>publicId</th><th>상태</th><th>작업</th></tr></thead>
              <tbody>
                {events.map((e:any) => {
                  const status = (e.status || e.state || '').toString()
                  const key = e.publicId || e.public_id || e.id
                  const cls = /closed|end|ended|CLOSED/i.test(status) ? 'closed' : (/open|running|active|OPEN/i.test(status) ? 'open' : 'pending')
                  const label = status || '-'
                  return (
                    <tr key={key}>
                      <td>{e.name || e.title || '무명 이벤트'}</td>
                      <td>{key}</td>
                      <td><span className={`badge badge-${cls}`}>{label}</span></td>
                      <td><button onClick={() => openDashboard(key)}>열기</button></td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  )
}
