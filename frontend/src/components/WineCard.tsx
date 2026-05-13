import { Wine } from '../types'

interface WineCardProps {
  wine: Wine
  onSelect?: (id: number) => void
  isSelected?: boolean
}

const formatRating = (rating: number) => {
  if (!rating) {
    return 'No rating'
  }

  return rating.toFixed(1)
}

export const WineCard = ({ wine, onSelect, isSelected = false }: WineCardProps) => {
  return (
    <button
      type="button"
      onClick={() => onSelect?.(wine.id)}
      className={[
        'w-full border bg-[#100d0d] p-5 text-left hover:border-[#7a1f2b] hover:bg-[#151010]',
        isSelected ? 'border-[#7a1f2b]' : 'border-[#2a191b]',
      ].join(' ')}
    >
      <div className="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <p className="text-xs font-semibold uppercase tracking-[0.24em] text-[#d7a55d]">
            {wine.country} / {wine.year}
          </p>
          <h2 className="mt-2 break-words text-xl font-bold text-[#9f2f3e] sm:text-2xl">{wine.name}</h2>
          <p className="mt-2 text-sm uppercase tracking-[0.18em] text-[#c9b7ad]">
            {wine.type}
            {wine.grape && (
              <>
                <span aria-hidden="true" className="mx-2 text-[#d7a55d]">
                  &bull;
                </span>
                {wine.grape}
              </>
            )}
          </p>
        </div>

        <div className="border-t border-[#3a2024] pt-4 sm:min-w-32 sm:border-l sm:border-t-0 sm:pl-5 sm:pt-0 sm:text-right">
          <p className="text-2xl font-bold text-[#d7a55d] sm:text-3xl">
            {formatRating(wine.averageRating)}
          </p>
          <p className="mt-1 text-sm text-[#c9b7ad]">
            {wine.ratingCount} {wine.ratingCount === 1 ? 'rating' : 'ratings'}
          </p>
        </div>
      </div>
    </button>
  )
}
