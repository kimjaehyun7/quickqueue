import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './styles.css'

createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
)

// On initial load, capture accessToken and publicId from query params if backend redirected to '/'
{
  const params = new URLSearchParams(window.location.search)
  const token = params.get('accessToken')
  const publicId = params.get('publicId')
  if (token) {
    // lazy import to avoid circular
    import('./auth').then(m => {
      m.setAccessToken(token)
      // remove sensitive query from URL
      params.delete('accessToken')
      params.delete('publicId')
      const newUrl = window.location.pathname + (params.toString() ? '?' + params.toString() : '')
      window.history.replaceState({}, '', newUrl)
      if (publicId) window.location.href = `/admin/dashboard/${publicId}`
    })
  }
}
