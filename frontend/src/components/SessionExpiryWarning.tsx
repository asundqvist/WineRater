import { useEffect, useMemo, useState } from 'react'
import { AUTH_STATE_CHANGE_EVENT, authService } from '../services/authService'

const WARNING_THRESHOLD_MS = 10 * 60 * 1000

interface JwtPayload {
  exp?: number
}

const decodeJwtPayload = (token: string): JwtPayload | null => {
  const payload = token.split('.')[1]
  if (!payload) {
    return null
  }

  try {
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/')
    const paddedBase64 = base64.padEnd(base64.length + (4 - (base64.length % 4 || 4)), '=')
    return JSON.parse(window.atob(paddedBase64))
  } catch {
    return null
  }
}

const getTokenExpiryMs = (token: string | null) => {
  if (!token) {
    return null
  }

  const payload = decodeJwtPayload(token)
  return payload?.exp ? payload.exp * 1000 : null
}

const formatRemainingTime = (remainingMs: number) => {
  const totalSeconds = Math.max(0, Math.ceil(remainingMs / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60

  if (minutes >= 1) {
    return `${minutes} min ${seconds.toString().padStart(2, '0')} sek`
  }

  return `${seconds} sek`
}

export const SessionExpiryWarning = () => {
  const [token, setToken] = useState(() => authService.getToken())
  const [now, setNow] = useState(() => Date.now())

  const expiresAt = useMemo(() => getTokenExpiryMs(token), [token])
  const remainingMs = expiresAt ? expiresAt - now : null
  const shouldShowWarning =
    remainingMs !== null && remainingMs > 0 && remainingMs <= WARNING_THRESHOLD_MS

  useEffect(() => {
    const syncToken = () => {
      setToken(authService.getToken())
      setNow(Date.now())
    }

    window.addEventListener('storage', syncToken)
    window.addEventListener(AUTH_STATE_CHANGE_EVENT, syncToken)

    return () => {
      window.removeEventListener('storage', syncToken)
      window.removeEventListener(AUTH_STATE_CHANGE_EVENT, syncToken)
    }
  }, [])

  useEffect(() => {
    if (!expiresAt) {
      return
    }

    const intervalId = window.setInterval(() => {
      setNow(Date.now())
    }, 1000)

    const logoutTimeoutId = window.setTimeout(() => {
      authService.logout()
      window.location.href = '/login'
    }, Math.max(0, expiresAt - Date.now()))

    return () => {
      window.clearInterval(intervalId)
      window.clearTimeout(logoutTimeoutId)
    }
  }, [expiresAt])

  if (!shouldShowWarning || remainingMs === null) {
    return null
  }

  return (
    <div className="fixed inset-x-4 bottom-4 z-50 border border-[#7a1f2b] bg-[#100d0d] px-5 py-4 shadow-2xl shadow-black/40 sm:left-auto sm:right-5 sm:max-w-sm">
      <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[#d7a55d]">
        Session expires soon
      </p>
      <p className="mt-2 text-sm leading-6 text-[#c9b7ad]">
        Du loggas ut automatiskt om {formatRemainingTime(remainingMs)}.
      </p>
    </div>
  )
}
