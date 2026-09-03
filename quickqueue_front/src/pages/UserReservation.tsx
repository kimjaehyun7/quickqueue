import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { createReservation, adminGetEvent } from '../api/client'

export default function UserReservation() {
  const [name, setName] = useState('')
  const [people, setPeople] = useState(1)
  const [phone, setPhone] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const params = useParams()
  const [publicId, setPublicId] = useState(params.publicId || 'demo')
  const [publicMode] = useState(!!params.publicId)

  const [eventName, setEventName] = useState('')
  const [eventStatus, setEventStatus] = useState('')
  const isClosed = /closed|end|ended|CLOSED/i.test(eventStatus || '')

  useEffect(() => {
    if (params.publicId) setPublicId(params.publicId)
    if (params.publicId) {
      ;(async () => {
        try {
          const ev = await adminGetEvent(params.publicId as string)
          setEventName(ev?.name || ev?.title || '')
          setEventStatus(ev?.status || ev?.state || '')
        } catch (err) {
          // ignore if backend requires auth
        }
      })()
    }
  }, [params.publicId])
  const [loading, setLoading] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setSuccessMessage('')
    try {
      const body = { representativeName: name, peopleCount: people, phoneNumber: phone }
      await createReservation(publicId, body)
      setName('')
      setPeople(1)
      setPhone('')
      setSuccessMessage('예약이 완료되었습니다. 문자로 전송된 링크에서 예약 현황을 확인해주세요.')
    } catch (err) {
      alert('예약에 실패했습니다.')
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card centered">
      <h1>{eventName || '예약하기'}</h1>
      {successMessage && <div className="success-note" role="status">{successMessage}</div>}
      <form onSubmit={submit} className="form">
        {!publicMode && (
          <>
            <label>이벤트 ID (공개용)</label>
            <input value={publicId} onChange={e => setPublicId(e.target.value)} />
          </>
        )}

        <label>대표자명</label>
        <input value={name} onChange={e => setName(e.target.value)} required />

        <label>인원수</label>
        <input type="number" value={people} onChange={e => setPeople(Number(e.target.value))} min={1} required />

        <label>전화번호</label>
        <input value={phone} onChange={e => setPhone(e.target.value)} required placeholder="01012341234" />

        {isClosed ? (
          <div className="muted-note">이벤트가 종료되어 더 이상 예약할 수 없습니다.</div>
        ) : (
          <button className="primary" disabled={loading}>{loading ? '예약 중...' : '예약'}</button>
        )}
      </form>
    </div>
  )
}
