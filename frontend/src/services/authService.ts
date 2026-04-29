import api from './api'
import { LoginRequest, AuthResponse, User } from '../types'

const AUTH_BASE_URL = 'http://localhost:8080/auth'

export interface RegisterRequest {
  username: string
  password: string
}

export const authService = {
  // Register
  register: async (credentials: RegisterRequest): Promise<void> => {
    await api.post(`${AUTH_BASE_URL}/register`, credentials)
  },

  // Login
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post(`${AUTH_BASE_URL}/login`, credentials)
    const data = response.data
    console.log('Raw API response:', data)

    // Store token and user info
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify({
      username: data.username,
      roles: data.roles,
    }))

    return data
  },

  // Logout
  logout: (): void => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  },

  // Get current user
  getCurrentUser: (): User | null => {
    const userStr = localStorage.getItem('user')
    if (!userStr) {
      return null
    }
    try {
      return JSON.parse(userStr)
    } catch {
      return null
    }
  },

  // Check if user is authenticated
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('token')
  },

  // Get token
  getToken: (): string | null => {
    return localStorage.getItem('token')
  },

  // Check if user has a specific role
  hasRole: (role: string): boolean => {
    const user = authService.getCurrentUser()
    return user?.roles?.includes(role) ?? false
  },
}
