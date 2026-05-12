import axios from 'axios'
import { API_ROOT_URL } from './api'
import { LoginRequest, AuthResponse, User } from '../types'

export const AUTH_STATE_CHANGE_EVENT = 'winerater-auth-state-change'

const AUTH_BASE_URL = import.meta.env.VITE_AUTH_BASE_URL ?? `${API_ROOT_URL}/auth`

const authApi = axios.create({
  baseURL: AUTH_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface RegisterRequest {
  username: string
  password: string
}

interface UsernameAvailabilityResponse {
  available: boolean
}

export const authService = {
  // Check username availability
  isUsernameAvailable: async (username: string): Promise<boolean> => {
    const response = await authApi.get<UsernameAvailabilityResponse>('/username-available', {
      params: { username },
    })
    return response.data.available
  },

  // Register
  register: async (credentials: RegisterRequest): Promise<void> => {
    await authApi.post('/register', credentials)
  },

  // Login
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await authApi.post('/login', credentials)
    const data = response.data
    console.log('Raw API response:', data)

    // Store token and user info for the current browser session.
    sessionStorage.setItem('token', data.token)
    sessionStorage.setItem('user', JSON.stringify({
      id: 0,
      username: data.username,
      roles: data.roles,
    }))
    window.dispatchEvent(new Event(AUTH_STATE_CHANGE_EVENT))

    return data
  },

  // Logout
  logout: (): void => {
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('user')
    window.dispatchEvent(new Event(AUTH_STATE_CHANGE_EVENT))
  },

  // Get current user
  getCurrentUser: (): User | null => {
    const userStr = sessionStorage.getItem('user')
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
    return !!sessionStorage.getItem('token')
  },

  // Get token
  getToken: (): string | null => {
    return sessionStorage.getItem('token')
  },

  // Check if user has a specific role
  hasRole: (role: string): boolean => {
    const user = authService.getCurrentUser()
    return user?.roles?.includes(role) ?? false
  },
}
