import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export const Navbar = () => {
  const navigate = useNavigate()
  const { user, logout, isAuthenticated } = useAuth()

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    [
      'rounded-full px-4 py-2 text-xs font-semibold uppercase tracking-[0.16em] sm:text-sm sm:tracking-[0.18em]',
      'text-[#9f2f3e] hover:bg-[#1c1717] hover:text-[#d7a55d]',
      isActive ? 'bg-[#211818] text-[#d7a55d]' : '',
    ].join(' ')

  return (
    <nav className="border-b border-[#2a191b] bg-[#090808]/95 backdrop-blur">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex min-h-20 flex-col items-center gap-4 py-4 md:flex-row md:items-center md:justify-between">
          <button
            onClick={() => navigate('/')}
            className="flex items-center justify-center gap-3 text-center md:justify-start md:text-left"
          >
            <span className="grid h-11 w-11 place-items-center rounded-full border border-[#7a1f2b] bg-[#130f0f] text-sm font-bold text-[#d7a55d]">
              WR
            </span>
            <span className="text-lg font-bold uppercase tracking-[0.22em] text-[#8f2634] sm:text-xl sm:tracking-[0.26em]">
              Wine Rater
            </span>
          </button>

          <div className="flex w-full flex-col items-center gap-4 sm:flex-row sm:items-center sm:justify-between md:flex-1">
            <div className="flex w-full flex-wrap justify-center gap-2 sm:w-auto md:justify-center">
              <NavLink to="/wines" className={navLinkClass}>
                Wine List
              </NavLink>
              <NavLink to="/vineyards" className={navLinkClass}>
                Vineyards
              </NavLink>
            </div>

            {isAuthenticated && user ? (
              <div className="flex flex-wrap items-center justify-center gap-3 sm:justify-end">
                <span className="text-center text-sm text-[#c9b7ad] sm:text-left">
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
              <div className="flex w-full gap-3 sm:w-auto">
                <button
                  onClick={() => navigate('/login')}
                  className="flex-1 rounded-full border border-[#7a1f2b] px-5 py-2 text-sm font-semibold text-[#b84a58] hover:border-[#d7a55d] hover:text-[#d7a55d] sm:flex-none"
                >
                  Login
                </button>
                <button
                  onClick={() => navigate('/register')}
                  className="flex-1 rounded-full bg-[#7a1f2b] px-5 py-2 text-sm font-semibold text-[#120d0e] hover:bg-[#d7a55d] sm:flex-none"
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
