import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService } from '../services/authService'
import { User } from '../types'

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(() => {
    // Initialize from localStorage immediately
    return authService.getCurrentUser()
  })
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    // Check localStorage on mount to ensure state is current
    const storedUser = authService.getCurrentUser()
    setUser(storedUser)
  }, [])

  // Listen for storage changes (e.g., from other tabs)
  useEffect(() => {
    const handleStorageChange = () => {
      const storedUser = authService.getCurrentUser()
      setUser(storedUser)
    }

    window.addEventListener('storage', handleStorageChange)
    return () => window.removeEventListener('storage', handleStorageChange)
  }, [])

  const login = async (username: string, password: string) => {
    setIsLoading(true)
    try {
      const response = await authService.login({ username, password })
      console.log('Login response:', response)
      const newUser: User = {
        username: response.username,
        roles: response.roles,
        id: 0,
      }
      setUser(newUser)
      return { success: true }
    } catch (error) {
      console.error('Auth service login error:', error)
      return {
        success: false,
        error: error instanceof Error ? error.message : 'Login failed',
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
