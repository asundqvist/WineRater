export interface Wine {
  id: number
  name: string
  type: string
  country: string
  year: number
  description?: string
}

export interface WineDetails extends Wine {
  ratings?: Rating[]
}

export interface Rating {
  id: number
  score: number
  comment: string
  createdAt: string
  user?: {
    id: number
    username: string
  }
}

export interface CreateRatingRequest {
  score: number
  comment: string
}

export interface CreateWineRequest {
  name: string
  type: string
  country: string
  year: number
  description?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  username: string
  roles: string[]
}

export interface User {
  id: number
  username: string
  roles: string[]
}

export interface ApiError {
  message: string
  status?: number
}

