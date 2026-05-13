export interface Wine {
  id: number
  name: string
  type: string
  grape?: string
  country: string
  year: number
  averageRating: number
  ratingCount: number
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
  username?: string
  user?: {
    id: number
    username: string
  }
}

export interface CreateRatingRequest {
  score: number
  comment: string
  wineId: number
}

export interface CreateWineRequest {
  name: string
  type: string
  grape?: string
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

export interface DailyWeather {
  date: string
  minTemperature: number
  maxTemperature: number
  weatherCode: number
}

export interface Weather {
  city: string
  country: string
  time: string
  temperature: number
  windSpeed: number
  weatherCode: number
  source: string
  dailyForecast: DailyWeather[]
}

