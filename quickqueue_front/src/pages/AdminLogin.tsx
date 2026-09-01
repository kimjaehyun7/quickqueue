import React, { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAccessToken } from '../auth'
import { adminGetEvents } from '../api/client'

export default function AdminLogin() {
  const navigate = useNavigate()

  useEffect(() => {
    const trySession = async () => {
      try {
        // Try calling an admin endpoint — if backend uses cookie session, this will succeed
        await adminGetEvents()
        navigate('/admin/dashboard')
        return
      } catch (err) {
        // ignore
      }

      if (getAccessToken()) {
        navigate('/admin/dashboard')
      }
    }
    trySession()
  }, [])

  const handleKakao = () => {
    // Redirect to backend OAuth endpoint (use VITE_API_BASE_URL)
    const backend = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080'
    const url = `${backend.replace(/\/$/, '')}/oauth2/authorization/kakao`
    window.location.href = url
  }

  return (
    <div className="card centered">
      <h1>관리자 로그인</h1>
      <button className="primary" style={{ padding: '16px 22px', width: '100%' }} onClick={handleKakao}>카카오 로그인</button>
    </div>
  )
}
