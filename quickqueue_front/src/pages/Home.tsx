import React, { useState } from 'react'
import { getAccessToken, clearAccessToken } from '../auth'
import { adminCreateEvent } from '../api/client'
import { useNavigate } from 'react-router-dom'

export default function Home() {
  const backend = (import.meta.env.VITE_API_BASE_URL as string) || 'http://16.176.178.31:8080'
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleKakao = () => {
    const url = `${backend.replace(/\/$/, '')}/oauth2/authorization/kakao`
    window.location.href = url
  }

  const handleCreateEvent = async () => {
    const name = prompt('이벤트명을 입력하세요')
    if (!name) return
    setLoading(true)
    try {
      const res: any = await adminCreateEvent({ name })
      if (res && (res.publicId || res.public_id || res.id)) {
        const publicId = res.publicId || res.public_id || res.id
        alert(`이벤트 생성됨: ${publicId}`)
        navigate(`/admin/dashboard/${publicId}`)
      } else {
        alert('이벤트 생성 응답을 확인하세요')
      }
    } catch (err) {
      console.error(err)
      alert('이벤트 생성에 실패했습니다')
    } finally { setLoading(false) }
  }

  const handleLogout = () => {
    clearAccessToken()
    window.location.reload()
  }

  const logged = !!getAccessToken()

  return (
    <div className="centered">
      <div className="card" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
        <h1>QuickQueue</h1>
        {!logged ? (
          <>
            <p style={{ color: '#6b7280' }}>관리자라면 아래 버튼을 눌러 로그인하세요.</p>
            <button className="primary" style={{ padding: '18px 28px', fontSize: 18, width: '100%' }} onClick={handleKakao}>관리자 로그인</button>
            <p style={{ marginTop: 8, color: '#9ca3af' }}>공용 예약 페이지는 이벤트가 생성된 후에 사용 가능합니다.</p>
          </>
        ) : (
          <>
            <h2>환영합니다, 관리자님</h2>
            <p style={{ color: '#6b7280' }}>새 이벤트를 생성하거나 대시보드로 이동하세요.</p>
            <div style={{ display: 'flex', gap: 8, width: '100%' }}>
              <button className="primary" style={{ flex: 1 }} onClick={handleCreateEvent} disabled={loading}>{loading ? '생성 중...' : '이벤트 생성'}</button>
              <button style={{ flex: 1 }} onClick={() => navigate('/admin/dashboard')}>대시보드</button>
            </div>
            <button style={{ marginTop: 8 }} onClick={handleLogout}>로그아웃</button>
          </>
        )}
      </div>
    </div>
  )
}
