import { useEffect, useMemo, useState } from 'react'
import { CreateWineRequest, Rating, Wine } from '../types'
import { wineService } from '../services/wineService'
import { WineCard } from '../components/WineCard'
import { handleApiError } from '../services/api'
import { useAuth } from '../hooks/useAuth'

const normalize = (value: string | number) => value.toString().toLowerCase().trim()
const isPresent = (value: string | undefined): value is string => Boolean(value)
const PAGE_SIZE = 10
const WINE_TYPES = [
  { value: 'Red', label: 'Red' },
  { value: 'White', label: 'White' },
  { value: 'Rose', label: 'Rosé' },
  { value: 'Sparkling', label: 'Sparkling' },
]

export const WinesPage = () => {
  const [wines, setWines] = useState<Wine[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [typeFilter, setTypeFilter] = useState('all')
  const [grapeFilter, setGrapeFilter] = useState('all')
  const [countryFilter, setCountryFilter] = useState('all')
  const [currentPage, setCurrentPage] = useState(1)
  const [selectedWineId, setSelectedWineId] = useState<number | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSavingWine, setIsSavingWine] = useState(false)
  const [isSavingRating, setIsSavingRating] = useState(false)
  const [isDeletingWine, setIsDeletingWine] = useState(false)
  const [isLoadingRatings, setIsLoadingRatings] = useState(false)
  const [showAddWine, setShowAddWine] = useState(false)
  const [showEditWine, setShowEditWine] = useState(false)
  const [showRatingForm, setShowRatingForm] = useState(false)
  const [showRatings, setShowRatings] = useState(false)
  const [ratings, setRatings] = useState<Rating[]>([])
  const [ratingScore, setRatingScore] = useState('5.0')
  const [ratingComment, setRatingComment] = useState('')
  const [error, setError] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isFeedbackVisible, setIsFeedbackVisible] = useState(false)
  const { isAuthenticated, hasRole } = useAuth()
  const isAdmin = hasRole('ROLE_ADMIN')

  useEffect(() => {
    loadWines()
  }, [])

  useEffect(() => {
    if (!error && !successMessage) {
      setIsFeedbackVisible(false)
      return
    }

    setIsFeedbackVisible(true)

    const fadeTimeoutId = window.setTimeout(() => {
      setIsFeedbackVisible(false)
    }, 3500)

    const removeTimeoutId = window.setTimeout(() => {
      setError('')
      setSuccessMessage('')
    }, 4000)

    return () => {
      window.clearTimeout(fadeTimeoutId)
      window.clearTimeout(removeTimeoutId)
    }
  }, [error, successMessage])

  const loadWines = async () => {
    setIsLoading(true)
    setError('')

    try {
      const data = await wineService.getAllWines()
      setWines(data)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsLoading(false)
    }
  }

  const handleAddWine = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const form = event.currentTarget
    setError('')
    setSuccessMessage('')
    setIsSavingWine(true)

    const formData = new FormData(form)
    const wine: CreateWineRequest = {
      name: String(formData.get('name') ?? '').trim(),
      type: String(formData.get('type') ?? '').trim(),
      grape: String(formData.get('grape') ?? '').trim() || undefined,
      country: String(formData.get('country') ?? '').trim(),
      year: Number(formData.get('year')),
    }

    try {
      const createdWine = await wineService.createWine(wine)
      setWines((current) => [...current, createdWine])
      setSelectedWineId(createdWine.id)
      setShowAddWine(false)
      form.reset()
      setSuccessMessage(`${createdWine.name} was added`)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsSavingWine(false)
    }
  }

  const handleUpdateWine = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!selectedWine) {
      return
    }

    const form = event.currentTarget
    const formData = new FormData(form)
    const wine: CreateWineRequest = {
      name: String(formData.get('name') ?? '').trim(),
      type: String(formData.get('type') ?? '').trim(),
      grape: String(formData.get('grape') ?? '').trim() || undefined,
      country: String(formData.get('country') ?? '').trim(),
      year: Number(formData.get('year')),
    }

    setError('')
    setSuccessMessage('')
    setIsSavingWine(true)

    try {
      const updatedWine = await wineService.updateWine(selectedWine.id, wine)
      setWines((current) => current.map((item) => (item.id === updatedWine.id ? updatedWine : item)))
      setSelectedWineId(updatedWine.id)
      setShowEditWine(false)
      setSuccessMessage(`${updatedWine.name} was updated`)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsSavingWine(false)
    }
  }

  const handleDeleteWine = async () => {
    if (!selectedWine) {
      return
    }

    const shouldDelete = window.confirm(`Delete ${selectedWine.name}? This cannot be undone.`)
    if (!shouldDelete) {
      return
    }

    setError('')
    setSuccessMessage('')
    setIsDeletingWine(true)

    try {
      await wineService.deleteWine(selectedWine.id)
      setWines((current) => current.filter((wine) => wine.id !== selectedWine.id))
      setSelectedWineId(null)
      setShowEditWine(false)
      setShowRatingForm(false)
      setShowRatings(false)
      setRatings([])
      setSuccessMessage(`${selectedWine.name} was deleted`)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsDeletingWine(false)
    }
  }

  const handleAddRating = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    if (!selectedWine) {
      return
    }

    setError('')
    setSuccessMessage('')
    setIsSavingRating(true)

    try {
      await wineService.addRating(selectedWine.id, {
        wineId: selectedWine.id,
        score: Number(ratingScore),
        comment: ratingComment.trim(),
      })

      await loadWines()
      setSelectedWineId(selectedWine.id)
      setShowRatingForm(false)
      setRatingScore('5.0')
      setRatingComment('')
      setSuccessMessage(`Rating added for ${selectedWine.name}`)
      if (showRatings) {
        await loadRatings(selectedWine.id)
      }
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsSavingRating(false)
    }
  }

  const loadRatings = async (wineId: number) => {
    setIsLoadingRatings(true)
    setError('')

    try {
      const data = await wineService.getWineRatings(wineId)
      setRatings(data)
      setShowRatings(true)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsLoadingRatings(false)
    }
  }

  const filteredWines = useMemo(() => {
    const query = normalize(searchTerm)
    const searchedWines = query
      ? wines.filter((wine) => normalize(wine.name).includes(query))
      : wines
    const visibleWines =
      typeFilter === 'all'
        ? searchedWines
        : searchedWines.filter((wine) => normalize(wine.type) === typeFilter)
    const grapeFilteredWines =
      grapeFilter === 'all'
        ? visibleWines
        : visibleWines.filter((wine) => wine.grape && normalize(wine.grape) === grapeFilter)
    const countryFilteredWines =
      countryFilter === 'all'
        ? grapeFilteredWines
        : grapeFilteredWines.filter((wine) => normalize(wine.country) === countryFilter)

    return [...countryFilteredWines].sort((a, b) => {
      return b.averageRating - a.averageRating || b.ratingCount - a.ratingCount || a.name.localeCompare(b.name)
    })
  }, [countryFilter, grapeFilter, searchTerm, typeFilter, wines])

  useEffect(() => {
    setCurrentPage(1)
  }, [countryFilter, grapeFilter, searchTerm, typeFilter])

  const wineGrapes = useMemo(
    () => Array.from(new Set(wines.map((wine) => wine.grape).filter(isPresent)))
      .sort((a, b) => a.localeCompare(b)),
    [wines]
  )

  const wineCountries = useMemo(
    () => Array.from(new Set(wines.map((wine) => wine.country).filter(Boolean)))
      .sort((a, b) => a.localeCompare(b)),
    [wines]
  )

  const totalPages = Math.max(1, Math.ceil(filteredWines.length / PAGE_SIZE))
  const pageStartIndex = (currentPage - 1) * PAGE_SIZE
  const paginatedWines = filteredWines.slice(pageStartIndex, pageStartIndex + PAGE_SIZE)

  useEffect(() => {
    if (currentPage > totalPages) {
      setCurrentPage(totalPages)
    }
  }, [currentPage, totalPages])

  const selectedWine = wines.find((wine) => wine.id === selectedWineId)
  const hasSearch = searchTerm.trim().length > 0

  return (
    <main className="flex flex-1 bg-[#080707]">
      <section className="mx-auto grid w-full max-w-7xl gap-8 px-4 py-10 sm:px-6 lg:grid-cols-[minmax(0,1fr)_360px] lg:px-8">
        <div>
          <div className="mb-8">
            <p className="text-sm font-semibold uppercase tracking-[0.35em] text-[#d7a55d]">
              Wine List
            </p>
            <div className="mt-4 flex flex-col gap-5 lg:flex-row lg:items-end lg:justify-between">
              <div>
                <h1 className="text-4xl font-bold text-[#8f2634] sm:text-5xl">
                  Browse wines
                </h1>
                <p className="mt-3 max-w-2xl text-[#c9b7ad]">
                  Search wine by name. Filter by type, grape and country.
                </p>
              </div>

              <button
                type="button"
                onClick={() => {
                  if (isAuthenticated) {
                    setShowAddWine((current) => !current)
                  } else {
                    setSelectedWineId(null)
                    setError('Sign in to add a wine')
                  }
                }}
                className="w-fit rounded-full border border-[#7a1f2b] px-5 py-2 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
              >
                {showAddWine ? 'Close form' : 'Add wine'}
              </button>
            </div>
          </div>

          {showAddWine && isAuthenticated && (
            <form
              onSubmit={handleAddWine}
              className="mb-6 grid gap-4 border border-[#2a191b] bg-[#0f0c0c] p-5 sm:grid-cols-2"
            >
              <div className="sm:col-span-2">
                <p className="text-sm font-semibold uppercase tracking-[0.24em] text-[#d7a55d]">
                  Add wine
                </p>
              </div>
              <WineInput name="name" label="Name" />
              <div>
                <label htmlFor="wine-type" className="block text-sm font-semibold text-[#d7a55d]">
                  Type
                </label>
                <select
                  id="wine-type"
                  name="type"
                  required
                  className="mt-2 w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none focus:border-[#7a1f2b]"
                >
                  {WINE_TYPES.map((type) => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </select>
              </div>
              <WineInput name="grape" label="Grape (optional)" required={false} />
              <WineInput name="country" label="Country" />
              <WineInput name="year" label="Year" type="number" />
              <button
                type="submit"
                disabled={isSavingWine}
                className="rounded-full bg-[#7a1f2b] px-6 py-3 font-semibold text-[#100b0c] hover:bg-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60 sm:col-span-2"
              >
                {isSavingWine ? 'Adding wine...' : 'Save wine'}
              </button>
            </form>
          )}

          <div className="mb-6 grid gap-4 border border-[#2a191b] bg-[#0f0c0c] p-4 lg:grid-cols-[minmax(0,1fr)_180px_180px_180px]">
            <label htmlFor="wine-search" className="sr-only">
              Search wines
            </label>
            <input
              id="wine-search"
              type="search"
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
              placeholder="Search wine"
              className="w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
            />
            <div>
              <label htmlFor="wine-type-filter" className="sr-only">
                Filter by type
              </label>
              <select
                id="wine-type-filter"
                value={typeFilter}
                onChange={(event) => setTypeFilter(event.target.value)}
                className="w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none focus:border-[#7a1f2b]"
              >
                <option value="all">All types</option>
                {WINE_TYPES.map((type) => (
                  <option key={type.value} value={normalize(type.value)}>
                    {type.label}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="wine-grape-filter" className="sr-only">
                Filter by grape
              </label>
              <select
                id="wine-grape-filter"
                value={grapeFilter}
                onChange={(event) => setGrapeFilter(event.target.value)}
                className="w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none focus:border-[#7a1f2b]"
              >
                <option value="all">All grapes</option>
                {wineGrapes.map((grape) => (
                  <option key={grape} value={normalize(grape)}>
                    {grape}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label htmlFor="wine-country-filter" className="sr-only">
                Filter by country
              </label>
              <select
                id="wine-country-filter"
                value={countryFilter}
                onChange={(event) => setCountryFilter(event.target.value)}
                className="w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none focus:border-[#7a1f2b]"
              >
                <option value="all">All countries</option>
                {wineCountries.map((country) => (
                  <option key={country} value={normalize(country)}>
                    {country}
                  </option>
                ))}
              </select>
            </div>
          </div>

          <p className="-mt-3 mb-4 text-sm italic text-[#d7a55d]">
            Sorted by top rated
          </p>

          {error && (
            <div
              className={[
                'mb-6 border border-[#7a1f2b] bg-[#1b1012] px-4 py-3 text-[#d7a55d] transition-opacity duration-500',
                isFeedbackVisible ? 'opacity-100' : 'opacity-0',
              ].join(' ')}
            >
              <div className="flex items-center justify-between gap-4">
                <p>{error}</p>
                <button
                  type="button"
                  onClick={() => {
                    setIsFeedbackVisible(false)
                    window.setTimeout(() => setError(''), 500)
                  }}
                  className="text-sm font-semibold text-[#b84a58] hover:text-[#d7a55d]"
                >
                  Close
                </button>
              </div>
            </div>
          )}

          {successMessage && (
            <div
              className={[
                'mb-6 border border-[#466b3d] bg-[#111a10] px-4 py-3 text-[#b9d79f] transition-opacity duration-500',
                isFeedbackVisible ? 'opacity-100' : 'opacity-0',
              ].join(' ')}
            >
              <div className="flex items-center justify-between gap-4">
                <p>{successMessage}</p>
                <button
                  type="button"
                  onClick={() => {
                    setIsFeedbackVisible(false)
                    window.setTimeout(() => setSuccessMessage(''), 500)
                  }}
                  className="text-sm font-semibold text-[#d7a55d] hover:text-[#b84a58]"
                >
                  Close
                </button>
              </div>
            </div>
          )}

          {isLoading ? (
            <div className="border border-[#2a191b] bg-[#0f0c0c] p-10 text-center text-[#c9b7ad]">
              Loading wines...
            </div>
          ) : filteredWines.length === 0 ? (
            <div className="border border-[#2a191b] bg-[#0f0c0c] p-10 text-center">
              <p className="text-lg font-semibold text-[#9f2f3e]">
                {hasSearch ? 'No wines match your search' : 'No wines available'}
              </p>
              {hasSearch && (
                <button
                  type="button"
                  onClick={() => setSearchTerm('')}
                  className="mt-4 rounded-full border border-[#7a1f2b] px-5 py-2 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
                >
                  Clear search
                </button>
              )}
            </div>
          ) : (
            <div className="space-y-4">
              {paginatedWines.map((wine) => (
                <WineCard
                  key={wine.id}
                  wine={wine}
                  isSelected={selectedWineId === wine.id}
                  onSelect={(wineId) => {
                    setSelectedWineId(wineId)
                    setShowEditWine(false)
                    setShowRatingForm(false)
                    setShowRatings(false)
                    setRatings([])
                    setRatingScore('5.0')
                    setRatingComment('')
                  }}
                />
              ))}
            </div>
          )}

          {filteredWines.length > PAGE_SIZE && (
            <div className="mt-6 flex flex-col gap-4 border border-[#2a191b] bg-[#0f0c0c] p-4 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm text-[#c9b7ad]">
                Showing {pageStartIndex + 1}-{Math.min(pageStartIndex + PAGE_SIZE, filteredWines.length)} of{' '}
                {filteredWines.length}
              </p>
              <div className="flex items-center gap-3">
                <button
                  type="button"
                  onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
                  disabled={currentPage === 1}
                  className="rounded-full border border-[#7a1f2b] px-4 py-2 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-40"
                >
                  Previous
                </button>
                <span className="text-sm font-semibold text-[#d7a55d]">
                  {currentPage} / {totalPages}
                </span>
                <button
                  type="button"
                  onClick={() => setCurrentPage((page) => Math.min(totalPages, page + 1))}
                  disabled={currentPage === totalPages}
                  className="rounded-full border border-[#7a1f2b] px-4 py-2 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-40"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>

        <aside className="h-fit border border-[#2a191b] bg-[#0f0c0c] p-6 lg:sticky lg:top-28">
          <p className="text-sm font-semibold uppercase tracking-[0.28em] text-[#d7a55d]">
            Summary
          </p>

          <div className="mt-6 grid grid-cols-2 gap-4">
            <div className="border border-[#2a191b] p-4">
              <p className="text-3xl font-bold text-[#9f2f3e]">{wines.length}</p>
              <p className="mt-1 text-sm text-[#c9b7ad]">Total wines</p>
            </div>
            <div className="border border-[#2a191b] p-4">
              <p className="text-3xl font-bold text-[#9f2f3e]">{paginatedWines.length}</p>
              <p className="mt-1 text-sm text-[#c9b7ad]">On this page</p>
            </div>
          </div>

          <div className="mt-6 border-t border-[#2a191b] pt-6">
            {selectedWine ? (
              <>
                <p className="text-sm uppercase tracking-[0.22em] text-[#d7a55d]">
                  Selected wine
                </p>
                <h2 className="mt-3 text-2xl font-bold text-[#9f2f3e]">
                  {selectedWine.name}
                </h2>
                <dl className="mt-5 space-y-3 text-sm text-[#c9b7ad]">
                  <div className="flex justify-between gap-4">
                    <dt>Type</dt>
                    <dd className="font-semibold text-[#d7a55d]">{selectedWine.type}</dd>
                  </div>
                  {selectedWine.grape && (
                    <div className="flex justify-between gap-4">
                      <dt>Grape</dt>
                      <dd className="font-semibold text-[#d7a55d]">{selectedWine.grape}</dd>
                    </div>
                  )}
                  <div className="flex justify-between gap-4">
                    <dt>Country</dt>
                    <dd className="font-semibold text-[#d7a55d]">{selectedWine.country}</dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt>Year</dt>
                    <dd className="font-semibold text-[#d7a55d]">{selectedWine.year}</dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt>Average rating</dt>
                    <dd className="font-semibold text-[#d7a55d]">
                      {selectedWine.averageRating
                        ? selectedWine.averageRating.toFixed(1)
                        : 'No rating'}
                    </dd>
                  </div>
                  <div className="flex justify-between gap-4">
                    <dt>Ratings</dt>
                    <dd className="font-semibold text-[#d7a55d]">{selectedWine.ratingCount}</dd>
                  </div>
                </dl>

                {isAdmin && (
                  <div className="mt-6 border-t border-[#2a191b] pt-6">
                    <p className="text-sm uppercase tracking-[0.22em] text-[#d7a55d]">
                      Admin
                    </p>

                    {showEditWine ? (
                      <form onSubmit={handleUpdateWine} className="mt-4 space-y-4">
                        <WineInput idPrefix="edit-wine" name="name" label="Name" defaultValue={selectedWine.name} />
                        <div>
                          <label htmlFor="edit-wine-type" className="block text-sm font-semibold text-[#d7a55d]">
                            Type
                          </label>
                          <select
                            id="edit-wine-type"
                            name="type"
                            required
                            defaultValue={selectedWine.type}
                            className="mt-2 w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none focus:border-[#7a1f2b]"
                          >
                            {WINE_TYPES.map((type) => (
                              <option key={type.value} value={type.value}>
                                {type.label}
                              </option>
                            ))}
                          </select>
                        </div>
                        <WineInput idPrefix="edit-wine" name="grape" label="Grape (optional)" defaultValue={selectedWine.grape} required={false} />
                        <WineInput idPrefix="edit-wine" name="country" label="Country" defaultValue={selectedWine.country} />
                        <WineInput idPrefix="edit-wine" name="year" label="Year" type="number" defaultValue={selectedWine.year} />

                        <div className="flex gap-3">
                          <button
                            type="button"
                            onClick={() => setShowEditWine(false)}
                            className="flex-1 rounded-full border border-[#7a1f2b] px-5 py-3 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
                          >
                            Cancel
                          </button>
                          <button
                            type="submit"
                            disabled={isSavingWine}
                            className="flex-1 rounded-full bg-[#7a1f2b] px-5 py-3 text-sm font-semibold text-[#100b0c] hover:bg-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
                          >
                            {isSavingWine ? 'Saving...' : 'Save'}
                          </button>
                        </div>
                      </form>
                    ) : (
                      <div className="mt-4 grid grid-cols-2 gap-3">
                        <button
                          type="button"
                          onClick={() => {
                            setShowEditWine(true)
                            setShowRatingForm(false)
                          }}
                          className="rounded-full border border-[#7a1f2b] px-5 py-3 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
                        >
                          Edit wine
                        </button>
                        <button
                          type="button"
                          onClick={handleDeleteWine}
                          disabled={isDeletingWine}
                          className="rounded-full border border-[#7a1f2b] px-5 py-3 text-sm font-semibold text-[#d7a55d] hover:border-[#b84a58] hover:text-[#b84a58] disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          {isDeletingWine ? 'Deleting...' : 'Delete'}
                        </button>
                      </div>
                    )}
                  </div>
                )}

                <div className="mt-6 border-t border-[#2a191b] pt-6">
                  <div className="flex flex-col gap-3">
                    <button
                      type="button"
                      onClick={() => loadRatings(selectedWine.id)}
                      disabled={isLoadingRatings}
                      className="w-full rounded-full border border-[#7a1f2b] px-6 py-3 font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
                    >
                      {isLoadingRatings ? 'Loading ratings...' : 'Show ratings'}
                    </button>

                    {showRatings && (
                      <button
                        type="button"
                        onClick={() => {
                          setShowRatings(false)
                          setRatings([])
                        }}
                        className="w-full text-sm font-semibold text-[#755f61] hover:text-[#d7a55d]"
                      >
                        Hide ratings
                      </button>
                    )}
                  </div>

                  {showRatings && (
                    <div className="mt-5 space-y-3">
                      {ratings.length === 0 ? (
                        <div className="border border-[#2a191b] bg-[#080707] p-4 text-sm text-[#c9b7ad]">
                          No ratings yet.
                        </div>
                      ) : (
                        ratings.map((rating) => (
                          <div key={rating.id} className="border border-[#2a191b] bg-[#080707] p-4">
                            <div className="flex items-start justify-between gap-4">
                              <div>
                                <p className="text-sm font-semibold text-[#d7a55d]">
                                  {rating.username || rating.user?.username || 'Unknown user'}
                                </p>
                                {rating.comment && (
                                  <p className="mt-2 text-sm leading-6 text-[#c9b7ad]">
                                    {rating.comment}
                                  </p>
                                )}
                              </div>
                              <p className="shrink-0 text-xl font-bold text-[#9f2f3e]">
                                {Number(rating.score).toFixed(1)}
                              </p>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  )}
                </div>

                <div className="mt-6 border-t border-[#2a191b] pt-6">
                  {isAuthenticated ? (
                    showRatingForm ? (
                      <form onSubmit={handleAddRating} className="space-y-4">
                        <h3 className="text-xl font-bold text-[#9f2f3e]">Add rating</h3>
                        <div>
                          <label htmlFor="rating-score" className="block text-sm font-semibold text-[#d7a55d]">
                            Rating
                          </label>
                          <input
                            id="rating-score"
                            type="number"
                            min="0"
                            max="5"
                            step="0.1"
                            inputMode="decimal"
                            value={ratingScore}
                            onChange={(event) => setRatingScore(event.target.value)}
                            className="mt-2 w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
                            placeholder="0.0 - 5.0"
                            required
                          />
                          <p className="mt-2 text-xs text-[#755f61]">
                            Use decimal format between 0.0 and 5.0.
                          </p>
                        </div>

                        <div>
                          <label htmlFor="rating-comment" className="block text-sm font-semibold text-[#d7a55d]">
                            Comment <span className="font-normal text-[#755f61]">(optional)</span>
                          </label>
                          <textarea
                            id="rating-comment"
                            value={ratingComment}
                            onChange={(event) => setRatingComment(event.target.value)}
                            rows={4}
                            maxLength={200}
                            placeholder="Write a short rating note"
                            className="mt-2 w-full resize-none border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
                          />
                        </div>

                        <div className="flex gap-3">
                          <button
                            type="button"
                            onClick={() => {
                              setShowRatingForm(false)
                              setRatingScore('5.0')
                              setRatingComment('')
                            }}
                            className="flex-1 rounded-full border border-[#7a1f2b] px-5 py-3 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
                          >
                            Cancel
                          </button>
                          <button
                            type="submit"
                            disabled={isSavingRating}
                            className="flex-1 rounded-full bg-[#7a1f2b] px-5 py-3 text-sm font-semibold text-[#100b0c] hover:bg-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
                          >
                            {isSavingRating ? 'Saving...' : 'Save rating'}
                          </button>
                        </div>
                      </form>
                    ) : (
                      <button
                        type="button"
                        onClick={() => setShowRatingForm(true)}
                        className="w-full rounded-full bg-[#7a1f2b] px-6 py-3 font-semibold text-[#100b0c] hover:bg-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
                      >
                        Add rating
                      </button>
                    )
                  ) : (
                    <div className="mt-4 border border-[#2a191b] bg-[#080707] p-4">
                      <p className="font-semibold text-[#d7a55d]">Sign in to make rating</p>
                      <p className="mt-2 text-sm text-[#c9b7ad]">
                        You can browse ratings without an account, but you need
                        to be signed in to rate a wine.
                      </p>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <p className="text-[#c9b7ad]">
                Select a wine to see details and add a rating.
              </p>
            )}
          </div>
        </aside>
      </section>
    </main>
  )
}

const WineInput = ({
  label,
  name,
  placeholder,
  defaultValue,
  idPrefix = 'wine',
  type = 'text',
  required = true,
}: {
  label: string
  name: string
  placeholder?: string
  defaultValue?: string | number
  idPrefix?: string
  type?: string
  required?: boolean
}) => (
  <div>
    <label htmlFor={`${idPrefix}-${name}`} className="block text-sm font-semibold text-[#d7a55d]">
      {label}
    </label>
    <input
      id={`${idPrefix}-${name}`}
      name={name}
      type={type}
      min={type === 'number' ? 1800 : undefined}
      max={type === 'number' ? new Date().getFullYear() : undefined}
      placeholder={placeholder}
      defaultValue={defaultValue}
      required={required}
      className="mt-2 w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
    />
  </div>
)
