import React, { useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { setAccessToken } from '../auth'

export default function AdminCallback() {
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    // Option 1: backend redirects with accessToken in query (for testing)
    const params = new URLSearchParams(location.search)
    const token = params.get('accessToken')
    const publicId = params.get('publicId')
    if (token) {
      setAccessToken(token)
      navigate(publicId ? `/admin/dashboard/${publicId}` : '/admin')
    } else {
      // If backend handles cookies only, redirect to admin home
      navigate('/admin')
    }
  }, [])

  return (
    <div className="card centered">
      <h2>로그인 처리 중...</h2>
      <p>잠시만 기다려 주세요.</p>
    </div>
  )
}
