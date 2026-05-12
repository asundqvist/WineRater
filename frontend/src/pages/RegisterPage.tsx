import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authService } from '../services/authService'
import { handleApiError } from '../services/api'

export const RegisterPage = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [usernameStatus, setUsernameStatus] = useState('')
  const [isCheckingUsername, setIsCheckingUsername] = useState(false)
  const navigate = useNavigate()

  const handleUsernameAvailabilityCheck = async () => {
    const trimmedUsername = username.trim()
    setUsernameStatus('')

    if (trimmedUsername.length < 3) {
      setUsernameStatus('Username must be at least 3 characters')
      return
    }

    setIsCheckingUsername(true)

    try {
      const isAvailable = await authService.isUsernameAvailable(trimmedUsername)
      setUsernameStatus(isAvailable ? 'Username is available' : 'Username is already in use')
    } catch {
      setUsernameStatus('Could not check username right now')
    } finally {
      setIsCheckingUsername(false)
    }
  }

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError('')
    setSuccess('')

    const trimmedUsername = username.trim()

    if (trimmedUsername.length < 3) {
      setError('Username must be at least 3 characters')
      return
    }

    if (usernameStatus === 'Username is already in use') {
      setError('Username is already in use')
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    setIsLoading(true)

    try {
      await authService.register({ username: trimmedUsername, password })
      setSuccess('Account created. Redirecting to login...')
      window.setTimeout(() => navigate('/login'), 4000)
    } catch (err) {
      const apiError = handleApiError(err)
      setError(apiError.message)
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <main className="relative isolate flex flex-1 items-center overflow-hidden bg-[#080707] px-4 py-16">
      <div
        className="absolute inset-0 -z-20 bg-cover bg-center opacity-35"
        style={{
          backgroundImage:
            "url('https://images.unsplash.com/photo-1506377247377-2a5b3b417ebb?auto=format&fit=crop&w=1800&q=80')",
        }}
      />
      <div className="absolute inset-0 -z-10 bg-gradient-to-r from-[#080707] via-[#080707]/95 to-[#080707]/80" />

      <section className="mx-auto grid w-full max-w-5xl gap-10 lg:grid-cols-[minmax(0,1fr)_420px] lg:items-center">
        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.35em] text-[#d7a55d]">
            Wine Rater
          </p>
          <h1 className="mt-5 text-5xl font-bold leading-tight text-[#8f2634]">
            Create your account.
          </h1>
          <p className="mt-5 max-w-xl text-lg leading-8 text-[#c9b7ad]">
            Are you a wine-lover? Register an account to become a part of the community!
          </p>
        </div>

        <div className="border border-[#2a191b] bg-[#0f0c0c]/95 p-6 shadow-2xl shadow-black/30">
          <h2 className="text-2xl font-bold text-[#9f2f3e]">Register</h2>
          <p className="mt-2 text-sm text-[#c9b7ad]">Create a Wine Rater user.</p>

          <form onSubmit={handleSubmit} className="mt-8 space-y-5">
            {error && (
              <div className="border border-[#7a1f2b] bg-[#1b1012] px-4 py-3 text-sm text-[#d7a55d]">
                {error}
              </div>
            )}

            {success && (
              <div className="border border-[#466b3d] bg-[#111a10] px-4 py-3 text-sm text-[#b9d79f]">
                {success}
              </div>
            )}

            <div>
              <label htmlFor="username" className="block text-sm font-semibold text-[#d7a55d]">
                Username
              </label>
              <div className="mt-2 flex flex-col gap-3 sm:flex-row">
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(event) => {
                    setUsername(event.target.value)
                    setUsernameStatus('')
                  }}
                  disabled={isLoading}
                  className="w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
                  placeholder="Choose username"
                  required
                />
                <button
                  type="button"
                  onClick={handleUsernameAvailabilityCheck}
                  disabled={isLoading || isCheckingUsername}
                  className="shrink-0 rounded-full border border-[#7a1f2b] px-5 py-3 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isCheckingUsername ? 'Checking...' : 'Check availability'}
                </button>
              </div>
              {(isCheckingUsername || usernameStatus) && (
                <p
                  className={[
                    'mt-2 text-sm',
                    usernameStatus === 'Username is available'
                      ? 'text-[#b9d79f]'
                      : 'text-[#d7a55d]',
                    usernameStatus === 'Username is already in use'
                      ? 'text-[#b84a58]'
                      : '',
                  ].join(' ')}
                >
                  {isCheckingUsername ? 'Checking username...' : usernameStatus}
                </p>
              )}
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
                placeholder="At least 6 characters"
                required
              />
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-semibold text-[#d7a55d]">
                Confirm password
              </label>
              <input
                id="confirmPassword"
                type="password"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                disabled={isLoading}
                className="mt-2 w-full border border-[#3a2024] bg-[#080707] px-4 py-3 text-[#c9b7ad] outline-none placeholder:text-[#755f61] focus:border-[#7a1f2b]"
                placeholder="Repeat password"
                required
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full rounded-full bg-[#7a1f2b] px-6 py-3 font-semibold text-[#100b0c] hover:bg-[#d7a55d] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoading ? 'Creating account...' : 'Create account'}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-[#c9b7ad]">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-[#d7a55d] hover:text-[#b84a58]">
              Sign in
            </Link>
          </p>
        </div>
      </section>
    </main>
  )
}
