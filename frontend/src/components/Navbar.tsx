import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export const Navbar = () => {
  const navigate = useNavigate()
  const { user, logout, isAuthenticated } = useAuth()

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    [
      'rounded-full px-4 py-2 text-sm font-semibold uppercase tracking-[0.18em]',
      'text-[#9f2f3e] hover:bg-[#1c1717] hover:text-[#d7a55d]',
      isActive ? 'bg-[#211818] text-[#d7a55d]' : '',
    ].join(' ')

  return (
    <nav className="border-b border-[#2a191b] bg-[#090808]/95 backdrop-blur">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex min-h-20 flex-col gap-4 py-4 md:flex-row md:items-center md:justify-between">
          <button
            onClick={() => navigate('/')}
            className="flex items-center gap-3 text-left"
          >
            <span className="grid h-11 w-11 place-items-center rounded-full border border-[#7a1f2b] bg-[#130f0f] text-sm font-bold text-[#d7a55d]">
              WR
            </span>
            <span className="text-xl font-bold uppercase tracking-[0.26em] text-[#8f2634]">
              Wine Rater
            </span>
          </button>

          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between md:flex-1">
            <div className="flex flex-wrap gap-2 md:justify-center">
              <NavLink to="/wines" className={navLinkClass}>
                Wine List
              </NavLink>
              <NavLink to="/vineyards" className={navLinkClass}>
                Vineyards
              </NavLink>
            </div>

            {isAuthenticated && user ? (
              <div className="flex flex-wrap items-center gap-3">
                <span className="text-sm text-[#c9b7ad]">
                  Signed in as: <span className="font-semibold text-[#d7a55d]">{user.username}</span>
                </span>
                <button
                  onClick={logout}
                  className="rounded-full border border-[#7a1f2b] px-5 py-2 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
                >
                  Logout
                </button>
              </div>
            ) : (
              <div className="flex gap-3">
                <button
                  onClick={() => navigate('/login')}
                  className="rounded-full border border-[#7a1f2b] px-5 py-2 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d]"
                >
                  Login
                </button>
                <button
                  onClick={() => navigate('/register')}
                  className="rounded-full bg-[#7a1f2b] px-5 py-2 text-sm font-semibold text-[#120d0e] hover:bg-[#d7a55d]"
                >
                  Register
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
