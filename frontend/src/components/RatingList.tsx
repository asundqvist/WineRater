import { Rating } from '../types'

interface RatingListProps {
  ratings: Rating[]
  isLoading?: boolean
}

export const RatingList = ({ ratings, isLoading = false }: RatingListProps) => {
  if (isLoading) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-500">Loading ratings...</p>
      </div>
    )
  }

  if (ratings.length === 0) {
    return (
      <div className="text-center py-8">
        <p className="text-gray-500">No ratings yet</p>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {ratings.map((rating) => (
        <div key={rating.id} className="bg-gray-50 rounded-lg p-4 border border-gray-200">
          <div className="flex justify-between items-start gap-4">
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2">
                <div className="flex gap-1">
                  {[...Array(5)].map((_, i) => (
                    <span key={i} className={i < rating.score ? 'text-yellow-400' : 'text-gray-300'}>
                      ★
                    </span>
                  ))}
                </div>
                <span className="font-semibold text-gray-900">{rating.score}/5</span>
              </div>
              <p className="text-gray-700 mb-2">{rating.comment}</p>
              <div className="text-xs text-gray-500">
                {rating.user?.username && <span>by {rating.user.username}</span>}
                {rating.user?.username && rating.createdAt && <span> • </span>}
                {rating.createdAt && <span>{new Date(rating.createdAt).toLocaleDateString()}</span>}
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  )
}

