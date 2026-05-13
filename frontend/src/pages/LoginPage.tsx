import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { handleApiError } from '../services/api'

export const LoginPage = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const { login } = useAuth()

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setIsLoading(true)

    try {
      const result = await login(username.trim(), password)
      if (result.success) {
        navigate('/wines')
        return
      }

      setError(result.error || 'Could not sign in')
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="relative isolate flex flex-1 items-center overflow-hidden bg-[#080707] px-4 py-10 sm:py-16">
      <div
        className="absolute inset-0 -z-20 bg-cover bg-center opacity-35"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?auto=format&fit=crop&w=1800&q=80')",
        }}
      />
      <div className="absolute inset-0 -z-10 bg-gradient-to-r from-[#080707] via-[#080707]/95 to-[#080707]/80" />

      <section className="mx-auto grid w-full max-w-5xl gap-10 lg:grid-cols-[minmax(0,1fr)_420px] lg:items-center">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[#d7a55d] sm:text-sm sm:tracking-[0.35em]">
            Wine Rater
          </p>
          <h1 className="mt-5 text-4xl font-bold leading-tight text-[#8f2634] sm:text-5xl">
            Welcome back!
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-[#c9b7ad] sm:text-lg sm:leading-8">
            Sign in to expand our collection with more wines and ratings!
          </p>
        </div>

        <div className="border border-[#2a191b] bg-[#0f0c0c]/95 p-4 shadow-2xl shadow-black/30 sm:p-6">
          <h2 className="text-2xl font-bold text-[#9f2f3e]">Login</h2>
          <p className="mt-2 text-sm text-[#c9b7ad]">Use your Wine Rater account.</p>

          <form onSubmit={handleSubmit} className="mt-8 space-y-5">
            {error && (
              <div className="border border-[#7a1f2b] bg-[#1b1012] px-4 py-3 text-sm text-[#d7a55d]">
                {error}
              </div>
            )}

            <div>
              <label htmlFor="username" className="block text-sm font-semibold text-[#d7a55d]">
                Username
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(event) => setUsername(event.target.value)}
                disabled={isLoading}
                className="mt-2 w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
                placeholder="Enter username"
                required
              />
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-semibold text-[#d7a55d]">
                Password
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
                disabled={isLoading}
                className="mt-2 w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
                placeholder="Enter password"
                required
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full rounded-full bg-[#7a1f2b] px-6 py-3 font-semibold text-[#100b0c] hover:bg-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoading ? 'Signing in...' : 'Sign in'}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-[#c9b7ad]">
            No account yet?{' '}
            <Link to="/register" className="font-semibold text-[#d7a55d] hover:text-[#b84a58]">
              Register
            </Link>
          </p>
        </div>
      </section>
    </main>
  )
}
