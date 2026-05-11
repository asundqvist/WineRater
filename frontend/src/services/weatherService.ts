import api from './api'
import { Weather } from '../types'

export const weatherService = {
  getWeather: async (city: string): Promise<Weather> => {
    const response = await api.get('/weather', {
      params: { city },
    })

    return response.data
  },
}
