import { useEffect, useMemo, useState } from 'react'
import { Weather } from '../types'
import { handleApiError } from '../services/api'
import { weatherService } from '../services/weatherService'

interface Vineyard {
  name: string
  city: string
  country: string
  region: string
  description: string
  imageUrl: string
}

interface VineyardWeatherState {
  data?: Weather
  error?: string
  isLoading: boolean
}

const vineyards: Vineyard[] = [
  {
    name: 'Chateau Margaux',
    city: 'Margaux',
    country: 'France',
    region: 'Bordeaux',
    description: 'A historic First Growth estate known for elegant Bordeaux wines.',
    imageUrl:
      'https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?auto=format&fit=crop&w=1400&q=80',
  },
  {
    name: 'Robert Mondavi Winery',
    city: 'Napa',
    country: 'United States',
    region: 'California',
    description: 'A Napa Valley landmark strongly associated with modern American wine.',
    imageUrl:
      'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?auto=format&fit=crop&w=1400&q=80',
  },
  {
    name: 'Vega Sicilia',
    city: 'Valbuena de Duero',
    country: 'Spain',
    region: 'Ribera del Duero',
    description: 'One of Spain’s most respected producers, based in Ribera del Duero.',
    imageUrl:
      'https://images.unsplash.com/photo-1528823872057-9c018a7a7553?auto=format&fit=crop&w=1400&q=80',
  },
  {
    name: 'Marchesi Antinori',
    city: 'Florence',
    country: 'Italy',
    region: 'Tuscany',
    description: 'A famous Tuscan wine family with centuries of winemaking history.',
    imageUrl:
      'https://images.unsplash.com/photo-1560493676-04071c5f467b?auto=format&fit=crop&w=1400&q=80',
  },
]

const describeWeather = (code: number) => {
  if (code === 0) return 'Clear sky'
  if ([1, 2, 3].includes(code)) return 'Partly cloudy'
  if ([45, 48].includes(code)) return 'Fog'
  if ([51, 53, 55, 56, 57].includes(code)) return 'Drizzle'
  if ([61, 63, 65, 66, 67].includes(code)) return 'Rain'
  if ([71, 73, 75, 77].includes(code)) return 'Snow'
  if ([80, 81, 82].includes(code)) return 'Showers'
  if ([95, 96, 99].includes(code)) return 'Thunderstorm'
  return 'Weather update'
}

const formatTemperature = (value: number) => `${Math.round(value)}\u00a0C`

export const VineyardsPage = () => {
  const [weatherByCity, setWeatherByCity] = useState<Record<string, VineyardWeatherState>>({})
  const [searchCity, setSearchCity] = useState('')
  const [searchedWeather, setSearchedWeather] = useState<Weather | null>(null)
  const [searchError, setSearchError] = useState('')
  const [isSearching, setIsSearching] = useState(false)

  const cityKeys = useMemo(() => vineyards.map((vineyard) => vineyard.city), [])

  useEffect(() => {
    setWeatherByCity(
      Object.fromEntries(
        cityKeys.map((city) => [city, { isLoading: true }])
      )
    )

    cityKeys.forEach(async (city) => {
      try {
        const data = await weatherService.getWeather(city)
        setWeatherByCity((current) => ({
          ...current,
          [city]: { data, isLoading: false },
        }))
      } catch (err) {
        const apiError = handleApiError(err)
        setWeatherByCity((current) => ({
          ...current,
          [city]: { error: apiError.message, isLoading: false },
        }))
      }
    })
  }, [cityKeys])

  const handleSearch = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const city = searchCity.trim()
    if (!city) {
      setSearchError('Enter a city to search')
      return
    }

    setIsSearching(true)
    setSearchError('')
    setSearchedWeather(null)

    try {
      const data = await weatherService.getWeather(city)
      setSearchedWeather(data)
    } catch (err) {
      const apiError = handleApiError(err)
      setSearchError(apiError.message)
    } finally {
      setIsSearching(false)
    }
  }

  return (
    <main className="flex flex-1 bg-[#080707]">
      <section className="mx-auto w-full max-w-7xl px-4 py-10 sm:px-6 lg:px-8">
        <div className="mb-10">
          <p className="text-sm font-semibold uppercase tracking-[0.35em] text-[#d7a55d]">
            Vineyards
          </p>
          <div className="mt-4 grid gap-8 lg:grid-cols-[minmax(0,1fr)_420px] lg:items-end">
            <div>
              <h1 className="text-4xl font-bold text-[#8f2634] sm:text-5xl">
                Vineyard weather
              </h1>
              <p className="mt-4 max-w-2xl text-[#c9b7ad]">
                Explore a few well-known wine estates and see current weather for
                their nearby cities. Weather data is fetched from the backend.
              </p>
            </div>

            <form onSubmit={handleSearch} className="border border-[#2a191b] bg-[#0f0c0c] p-4">
              <label htmlFor="weather-search" className="block text-sm font-semibold text-[#d7a55d]">
                Search weather by city
              </label>
              <div className="mt-3 flex flex-col gap-3 sm:flex-row">
                <input
                  id="weather-search"
                  type="search"
                  value={searchCity}
                  onChange={(event) => setSearchCity(event.target.value)}
                  className="min-w-0 flex-1 border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
                />
                <button
                  type="submit"
                  disabled={isSearching}
                  className="rounded-full bg-[#7a1f2b] px-6 py-3 font-semibold text-[#100b0c] hover:bg-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isSearching ? 'Searching...' : 'Search'}
                </button>
              </div>

              {(searchError || searchedWeather) && (
                <div className="mt-4 border-t border-[#2a191b] pt-4">
                  {searchError && <p className="text-sm text-[#d7a55d]">{searchError}</p>}
                  {searchedWeather && (
                    <WeatherSummary weather={searchedWeather} compact />
                  )}
                </div>
              )}
            </form>
          </div>
        </div>

        <div className="grid gap-6">
          {vineyards.map((vineyard) => (
            <VineyardBanner
              key={vineyard.name}
              vineyard={vineyard}
              weatherState={weatherByCity[vineyard.city] ?? { isLoading: true }}
            />
          ))}
        </div>
      </section>
    </main>
  )
}

const VineyardBanner = ({
  vineyard,
  weatherState,
}: {
  vineyard: Vineyard
  weatherState: VineyardWeatherState
}) => {
  return (
    <article className="relative isolate min-h-80 overflow-hidden border border-[#2a191b] bg-[#0f0c0c]">
      <div
        className="absolute inset-0 -z-20 bg-cover bg-center"
        style={{ backgroundImage: `url('${vineyard.imageUrl}')` }}
      />
      <div className="absolute inset-0 -z-10 bg-gradient-to-r from-[#080707] via-[#080707]/90 to-[#080707]/35" />
      <div className="grid min-h-80 gap-8 p-6 sm:p-8 lg:grid-cols-[minmax(0,1fr)_360px] lg:items-end">
        <div className="max-w-2xl self-end">
          <p className="text-sm font-semibold uppercase tracking-[0.3em] text-[#d7a55d]">
            {vineyard.region}
          </p>
          <h2 className="mt-4 text-4xl font-bold text-[#9f2f3e]">{vineyard.name}</h2>
          <p className="mt-3 text-lg text-[#c9b7ad]">
            {vineyard.city}, {vineyard.country}
          </p>
          <p className="mt-4 max-w-xl text-[#c9b7ad]">{vineyard.description}</p>
        </div>

        <div className="self-end border-l border-[#5b1b25] bg-[#090808]/75 p-5 backdrop-blur-sm">
          {weatherState.isLoading && (
            <p className="text-sm text-[#c9b7ad]">Loading current weather...</p>
          )}

          {weatherState.error && (
            <p className="text-sm text-[#d7a55d]">{weatherState.error}</p>
          )}

          {weatherState.data && <WeatherSummary weather={weatherState.data} />}
        </div>
      </div>
    </article>
  )
}

const WeatherSummary = ({
  weather,
  compact = false,
}: {
  weather: Weather
  compact?: boolean
}) => {
  return (
    <div>
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.22em] text-[#d7a55d]">
            Current weather
          </p>
          <h3 className="mt-2 text-2xl font-bold text-[#9f2f3e]">
            {weather.city}, {weather.country}
          </h3>
        </div>
        <p className="shrink-0 whitespace-nowrap text-4xl font-bold text-[#d7a55d]">
          {formatTemperature(weather.temperature)}
        </p>
      </div>

      <dl className="mt-5 grid grid-cols-2 gap-4 text-sm text-[#c9b7ad]">
        <div>
          <dt className="text-[#755f61]">Condition</dt>
          <dd className="mt-1 font-semibold text-[#d7a55d]">
            {describeWeather(weather.weatherCode)}
          </dd>
        </div>
        <div>
          <dt className="text-[#755f61]">Wind</dt>
          <dd className="mt-1 font-semibold text-[#d7a55d]">
            {Math.round(weather.windSpeed)} km/h
          </dd>
        </div>
      </dl>

      {!compact && weather.dailyForecast.length > 0 && (
        <div className="mt-5 border-t border-[#2a191b] pt-4">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-[#755f61]">
            3 day forecast
          </p>
          <div className="mt-3 space-y-2">
            {weather.dailyForecast.map((day) => (
              <div key={day.date} className="flex justify-between gap-4 text-sm">
                <span className="text-[#c9b7ad]">{day.date}</span>
                <span className="shrink-0 whitespace-nowrap font-semibold text-[#d7a55d]">
                  {formatTemperature(day.minTemperature)} / {formatTemperature(day.maxTemperature)}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      <p className="mt-4 text-xs uppercase tracking-[0.18em] text-[#755f61]">
        Source: {weather.source}
      </p>
    </div>
  )
}
