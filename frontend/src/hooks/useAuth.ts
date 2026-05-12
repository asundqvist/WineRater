import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AUTH_STATE_CHANGE_EVENT, authService } from '../services/authService'
import { handleApiError } from '../services/api'
import { User } from '../types'

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(() => {
    // Initialize from sessionStorage immediately
    return authService.getCurrentUser()
  })
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    // Check sessionStorage on mount to ensure state is current
    const storedUser = authService.getCurrentUser()
    setUser(storedUser)
  }, [])

  // Listen for auth changes from this tab and other tabs.
  useEffect(() => {
    const syncUser = () => {
      const storedUser = authService.getCurrentUser()
      setUser(storedUser)
    }

    window.addEventListener('storage', syncUser)
    window.addEventListener(AUTH_STATE_CHANGE_EVENT, syncUser)

    return () => {
      window.removeEventListener('storage', syncUser)
      window.removeEventListener(AUTH_STATE_CHANGE_EVENT, syncUser)
    }
  }, [])

  const login = async (username: string, password: string) => {
    setIsLoading(true)
    try {
      const response = await authService.login({ username, password })
      const newUser: User = {
        username: response.username,
        roles: response.roles,
        id: 0,
      }
      setUser(newUser)
      return { success: true }
    } catch (error) {
      console.error('Auth service login error:', error)
      const apiError = handleApiError(error)
      return {
        success: false,
        error: apiError.message,
      }
    } finally {
      setIsLoading(false)
    }
  }

  const logout = () => {
    authService.logout()
    setUser(null)
    navigate('/login')
  }

  const hasRole = (role: string): boolean => {
    return user?.roles?.includes(role) ?? false
  }

  return {
    user,
    isLoading,
    isAuthenticated: !!user,
    login,
    logout,
    hasRole,
  }
}
