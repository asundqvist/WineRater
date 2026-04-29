import { useState, useEffect } from 'react'
import { Wine, CreateWineRequest, CreateRatingRequest, Rating } from '../types'
import { wineService } from '../services/wineService'
import { WineCard } from '../components/WineCard'
import { RatingList } from '../components/RatingList'
import { useAuth } from '../hooks/useAuth'
import { handleApiError } from '../services/api'

export const WinesPage = () => {
  const [wines, setWines] = useState<Wine[]>([])
  const [isLoadingWines, setIsLoadingWines] = useState(true)
  const [error, setError] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedWineId, setSelectedWineId] = useState<number | null>(null)
  const [wineRatings, setWineRatings] = useState<Rating[]>([])
  const [isLoadingRatings, setIsLoadingRatings] = useState(false)
  const [showAddWine, setShowAddWine] = useState(false)
  const [showAddRating, setShowAddRating] = useState(false)
  const { user, isAuthenticated } = useAuth()

  // Load wines on mount
  useEffect(() => {
    loadWines()
  }, [])

  const loadWines = async () => {
    setIsLoadingWines(true)
    setError('')
    try {
      const data = await wineService.getAllWines()
      setWines(data)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsLoadingWines(false)
    }
  }

  const loadWineRatings = async (wineId: number) => {
    setIsLoadingRatings(true)
    try {
      const ratings = await wineService.getWineRatings(wineId)
      setWineRatings(ratings)
      setSelectedWineId(wineId)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsLoadingRatings(false)
    }
  }

  const handleDeleteWine = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this wine?')) {
      return
    }

    try {
      await wineService.deleteWine(id)
      setWines(wines.filter((w) => w.id !== id))
      setError('')
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    }
  }

  const handleAddWine = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    const formData = new FormData(e.currentTarget)

    const newWine: CreateWineRequest = {
      name: formData.get('name') as string,
      type: formData.get('type') as string,
      country: formData.get('country') as string,
      year: parseInt(formData.get('year') as string),
      description: (formData.get('description') as string) || undefined,
    }

    try {
      const wine = await wineService.createWine(newWine)
      setWines([...wines, wine])
      setShowAddWine(false)
      ;(e.target as HTMLFormElement).reset()
      setError('')
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    }
  }

  const handleAddRating = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault()
    if (!selectedWineId) return

    const formData = new FormData(e.currentTarget)
    const score = formData.get('score') as string
    const comment = formData.get('comment') as string

    if (!score || !comment) {
      setError('Please fill in both score and comment')
      return
    }

    const rating: CreateRatingRequest = {
      score: parseInt(score),
      comment: comment,
    }

    try {
      console.log('Adding rating:', rating, 'for wine:', selectedWineId)
      await wineService.addRating(selectedWineId, rating)
      await loadWineRatings(selectedWineId)
      setShowAddRating(false)
      ;(e.target as HTMLFormElement).reset()
      setError('')
    } catch (err) {
      console.error('Rating error:', err)
      const apiError = handleApiError(err)
      setError(apiError.message || 'Failed to add rating')
    }
  }

  const filteredWines = wines.filter(
    (wine) =>
      wine.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      wine.type.toLowerCase().includes(searchTerm.toLowerCase()) ||
      wine.country.toLowerCase().includes(searchTerm.toLowerCase())
  )

  if (isLoadingWines && wines.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <p className="text-gray-600 text-lg">Loading wines...</p>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Error Alert */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
            {error}
            <button
              onClick={() => setError('')}
              className="float-right text-red-600 hover:text-red-800"
            >
              ✕
            </button>
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content - Wines List */}
          <div className="lg:col-span-2">
            <div className="mb-8">
              <div className="flex justify-between items-center mb-4">
                <h1 className="text-3xl font-bold text-gray-900">Wines</h1>
                {isAuthenticated && user?.roles?.includes('ADMIN') && (
                  <button
                    onClick={() => setShowAddWine(!showAddWine)}
                    className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium"
                  >
                    {showAddWine ? 'Cancel' : '+ Add Wine'}
                  </button>
                )}
              </div>

              {/* Add Wine Form */}
              {showAddWine && (
                <div className="bg-white rounded-lg shadow border border-gray-200 p-6 mb-6">
                  <h2 className="text-lg font-semibold text-gray-900 mb-4">Add New Wine</h2>
                  <form onSubmit={handleAddWine} className="space-y-4">
                    <input
                      type="text"
                      name="name"
                      placeholder="Wine Name"
                      required
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-600 text-gray-900"
                    />
                    <input
                      type="text"
                      name="type"
                      placeholder="Type (e.g., Red, White)"
                      required
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-600 text-gray-900"
                    />
                    <input
                      type="text"
                      name="country"
                      placeholder="Country"
                      required
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-600 text-gray-900"
                    />
                    <input
                      type="number"
                      name="year"
                      placeholder="Year"
                      required
                      min="1900"
                      max={new Date().getFullYear()}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-600 text-gray-900"
                    />
                    <textarea
                      name="description"
                      placeholder="Description (optional)"
                      rows={3}
                      className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-green-600 text-gray-900"
                    />
                    <button
                      type="submit"
                      className="w-full px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium"
                    >
                      Add Wine
                    </button>
                  </form>
                </div>
              )}

              {/* Search */}
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search wines..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 text-gray-900 placeholder-gray-500"
                />
                <span className="absolute right-3 top-3 text-gray-400">🔍</span>
              </div>
            </div>

            {/* Wines List */}
            {isLoadingWines ? (
              <div className="text-center py-12">
                <p className="text-gray-500">Loading wines...</p>
              </div>
            ) : filteredWines.length === 0 ? (
              <div className="text-center py-12 bg-white rounded-lg shadow border border-gray-200">
                <p className="text-gray-500">
                  {wines.length === 0 ? 'No wines available' : 'No wines match your search'}
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {filteredWines.map((wine) => (
                  <WineCard
                    key={wine.id}
                    wine={wine}
                    onDelete={() => handleDeleteWine(wine.id)}
                    onRateClick={() => {
                      loadWineRatings(wine.id)
                      setShowAddRating(true)
                    }}
                    ratingCount={
                      selectedWineId === wine.id ? wineRatings.length : 0
                    }
                  />
                ))}
              </div>
            )}
          </div>

          {/* Sidebar - Ratings */}
          <div>
            {selectedWineId ? (
              <div className="bg-white rounded-lg shadow border border-gray-200 p-6 sticky top-6">
                <div className="flex justify-between items-start mb-4">
                  <h2 className="text-lg font-semibold text-gray-900">Ratings</h2>
                  <button
                    onClick={() => setSelectedWineId(null)}
                    className="text-gray-400 hover:text-gray-600"
                  >
                    ✕
                  </button>
                </div>

                {/* Add Rating Form */}
                {showAddRating && isAuthenticated && (
                  <form onSubmit={handleAddRating} className="mb-6 p-4 bg-blue-50 rounded-lg border border-blue-200">
                    <h3 className="font-medium text-gray-900 mb-3">Add Your Rating</h3>
                    <div className="space-y-3">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">
                          Score (1-5)
                        </label>
                        <select
                          name="score"
                          required
                          className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-gray-900"
                        >
                          <option value="">Select score</option>
                          <option value="1">1 - Poor</option>
                          <option value="2">2 - Fair</option>
                          <option value="3">3 - Good</option>
                          <option value="4">4 - Very Good</option>
                          <option value="5">5 - Excellent</option>
                        </select>
                      </div>
                      <textarea
                        name="comment"
                        placeholder="Your comment (required)"
                        rows={3}
                        required
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-600 text-gray-900"
                      />
                      <div className="flex gap-2">
                        <button
                          type="submit"
                          className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                        >
                          Submit
                        </button>
                        <button
                          type="button"
                          onClick={() => setShowAddRating(false)}
                          className="flex-1 px-3 py-2 bg-gray-200 text-gray-900 rounded-lg hover:bg-gray-300 transition-colors text-sm font-medium"
                        >
                          Cancel
                        </button>
                      </div>
                    </div>
                  </form>
                )}

                {/* Ratings List */}
                <RatingList ratings={wineRatings} isLoading={isLoadingRatings} />

                {!showAddRating && wineRatings.length > 0 && isAuthenticated && (
                  <button
                    onClick={() => setShowAddRating(true)}
                    className="w-full mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm font-medium"
                  >
                    Add Rating
                  </button>
                )}

                {!isAuthenticated && wineRatings.length > 0 && (
                  <div className="mt-4 p-3 bg-gray-50 rounded-lg border border-gray-200">
                    <p className="text-sm text-gray-600 text-center">
                      <a href="/login" className="text-blue-600 hover:underline font-medium">
                        Login
                      </a>{' '}
                      to add your own rating
                    </p>
                  </div>
                )}
              </div>
            ) : (
              <div className="bg-white rounded-lg shadow border border-gray-200 p-6">
                <p className="text-gray-500 text-center">Select a wine to view and add ratings</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

