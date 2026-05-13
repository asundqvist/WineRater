import api from './api'
import { Wine, WineDetails, CreateWineRequest, Rating, CreateRatingRequest } from '../types'

export const wineService = {
  // Get all wines
  getAllWines: async (): Promise<Wine[]> => {
    const response = await api.get('/wines')
    return response.data
  },

  // Get wine by ID with ratings
  getWineById: async (id: number): Promise<WineDetails> => {
    const response = await api.get(`/wines/${id}`)
    return response.data
  },

  // Get ratings for a wine
  getWineRatings: async (wineId: number): Promise<Rating[]> => {
    const response = await api.get(`/ratings/wine/${wineId}`)
    return response.data
  },

  // Create a new wine
  createWine: async (wine: CreateWineRequest): Promise<Wine> => {
    const response = await api.post('/wines', wine)
    return response.data
  },

  // Update a wine
  updateWine: async (id: number, wine: CreateWineRequest): Promise<Wine> => {
    const response = await api.put(`/wines/${id}`, wine)
    return response.data
  },

  // Delete a wine
  deleteWine: async (id: number): Promise<void> => {
    await api.delete(`/wines/${id}`)
  },

  // Delete a rating
  deleteRating: async (id: number): Promise<void> => {
    await api.delete(`/ratings/${id}`)
  },

  // Add a rating to a wine
  addRating: async (wineId: number, rating: CreateRatingRequest): Promise<Rating> => {
    const response = await api.post('/ratings', { ...rating, wineId })
    return response.data
  },
}

