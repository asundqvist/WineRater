import { useAuth } from '../hooks/useAuth'
import { useNavigate } from 'react-router-dom'

export const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
  }

  const handleLogoClick = () => {
    navigate('/wines')
  }

  return (
    <nav className="bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          <button
            onClick={handleLogoClick}
            className="flex items-center gap-2 hover:opacity-75 cursor-pointer"
          >
            <span className="text-2xl">🍷</span>
            <span className="font-bold text-xl text-gray-900">Wine Rater</span>
          </button>

          <div className="flex items-center gap-4">
            {isAuthenticated && user ? (
              <>
                <div className="flex items-center gap-3">
                  <div className="text-sm">
                    <p className="text-gray-600">Logged in as:</p>
                    <p className="font-semibold text-gray-900">{user.username}</p>
                  </div>
                  {user.roles?.includes('ADMIN') && (
                    <span className="text-xs bg-purple-100 text-purple-800 px-3 py-1 rounded-full font-medium">
                      Admin
                    </span>
                  )}
                </div>
                <button
                  onClick={handleLogout}
                  className="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors font-medium"
                >
                  Logout
                </button>
              </>
            ) : (
              <button
                onClick={() => navigate('/login')}
                className="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 transition-colors font-medium"
              >
                Login
              </button>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}
