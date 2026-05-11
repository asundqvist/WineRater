import { useState } from 'react'
import { Link } from 'react-router-dom'

const heroVideo = 'https://videos.pexels.com/video-files/3775895/3775895-hd_1920_1080_25fps.mp4'

export const HomePage = () => {
  const [hasVideoEnded, setHasVideoEnded] = useState(false)
  const posterImage =
    'https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?auto=format&fit=crop&w=1800&q=80'

  return (
    <main className="relative isolate flex flex-1 items-center overflow-hidden">
      <div
        className={[
          'absolute inset-0 -z-20 bg-cover bg-center transition-opacity duration-1000',
          hasVideoEnded ? 'opacity-100' : 'opacity-0',
        ].join(' ')}
        style={{ backgroundImage: `url('${posterImage}')` }}
      />
      <video
        className={[
          'absolute inset-0 -z-20 h-full w-full object-cover transition-opacity duration-1000',
          hasVideoEnded ? 'opacity-0' : 'opacity-100',
        ].join(' ')}
        autoPlay
        muted
        playsInline
        poster={posterImage}
        onEnded={() => setHasVideoEnded(true)}
      >
        <source
          src={heroVideo}
          type="video/mp4"
        />
      </video>
      <div className="absolute inset-0 -z-10 bg-gradient-to-r from-[#080707] via-[#080707]/90 to-[#080707]/55" />
      <div className="absolute inset-x-0 bottom-0 -z-10 h-48 bg-gradient-to-t from-[#080707] to-transparent" />

      <section className="mx-auto grid w-full max-w-7xl gap-10 px-4 py-20 sm:px-6 lg:grid-cols-[minmax(0,1fr)_420px] lg:px-8">
        <div className="max-w-3xl">
          <p className="mb-5 text-sm font-semibold uppercase tracking-[0.35em] text-[#d7a55d]">
            Wine Rater
          </p>
          <h1 className="text-5xl font-bold leading-tight text-[#8f2634] sm:text-6xl lg:text-7xl">
            Rate, discover and revisit memorable wines.
          </h1>
          <p className="mt-6 max-w-2xl text-lg leading-8 text-[#c9b7ad]">
            A focused wine journal for browsing bottles, comparing ratings and
            later pairing vineyards with local weather.
          </p>

          <div className="mt-10 flex flex-wrap gap-4">
            <Link
              to="/wines"
              className="rounded-full bg-[#7a1f2b] px-7 py-3 font-semibold text-[#100b0c] hover:bg-[#d7a55d]"
            >
              Wine List
            </Link>
            <Link
              to="/vineyards"
              className="rounded-full border border-[#7a1f2b] px-7 py-3 font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
            >
              Vineyards
            </Link>
          </div>
        </div>
      </section>
    </main>
  )
}
