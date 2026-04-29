import { Wine } from '../types'
import { useAuth } from '../hooks/useAuth'
import { useNavigate } from 'react-router-dom'

interface WineCardProps {
  wine: Wine
  onDelete?: (id: number) => void
  onRateClick?: (id: number) => void
  ratingCount?: number
}

export const WineCard = ({ wine, onDelete, onRateClick, ratingCount = 0 }: WineCardProps) => {
  const { hasRole, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const isAdmin = hasRole('ADMIN')

  const handleRateClick = () => {
    if (!isAuthenticated) {
      navigate('/login')
      return
    }
    if (onRateClick) {
      onRateClick(wine.id)
    }
  }

  return (
    <div className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow border border-gray-200 overflow-hidden">
      <div className="p-6">
        <div className="flex justify-between items-start gap-4">
          <div className="flex-1">
            <h3 className="text-lg font-semibold text-gray-900 mb-2">{wine.name}</h3>
            <div className="space-y-1 text-sm text-gray-600">
              <p>
                <span className="font-medium">Type:</span> {wine.type}
              </p>
              <p>
                <span className="font-medium">Country:</span> {wine.country}
              </p>
              <p>
                <span className="font-medium">Year:</span> {wine.year}
              </p>
            </div>

            {wine.description && (
              <p className="mt-3 text-gray-700 text-sm">{wine.description}</p>
            )}

            <div className="mt-4 text-xs text-gray-500">
              {ratingCount} {ratingCount === 1 ? 'rating' : 'ratings'}
            </div>
          </div>

          <div className="flex gap-2 flex-col">
            {onRateClick && (
              <button
                onClick={handleRateClick}
                className="px-3 py-1 bg-blue-100 text-blue-700 rounded hover:bg-blue-200 transition-colors text-sm font-medium whitespace-nowrap"
              >
                Add Rating
              </button>
            )}
            {isAdmin && onDelete && (
              <button
                onClick={() => onDelete(wine.id)}
                className="px-3 py-1 bg-red-100 text-red-700 rounded hover:bg-red-200 transition-colors text-sm font-medium whitespace-nowrap"
              >
                Delete
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
