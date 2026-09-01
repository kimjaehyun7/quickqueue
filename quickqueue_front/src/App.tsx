import React from 'react'
import { Routes, Route, Link } from 'react-router-dom'
import UserReservation from './pages/UserReservation'
import ReservationStatus from './pages/ReservationStatus'
import AdminLogin from './pages/AdminLogin'
import AdminDashboard from './pages/AdminDashboard'
import AdminCallback from './pages/AdminCallback'
import Home from './pages/Home'
import AdminHome from './pages/AdminHome'

export default function App() {
  return (
    <div className="app">
      <header className="app-header">
        <Link to="/" className="logo">QuickQueue</Link>
      </header>

      <main className="app-main">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/events/:publicId" element={<UserReservation />} />
          <Route path="/reservations/:token" element={<ReservationStatus />} />
          <Route path="/admin" element={<AdminLogin />} />
          <Route path="/admin/callback" element={<AdminCallback />} />
          <Route path="/admin/dashboard" element={<AdminHome />} />
          <Route path="/admin/dashboard/:publicId" element={<AdminDashboard />} />
        </Routes>
      </main>
    </div>
  )
}
