import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Navbar } from './components/Navbar'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { VineyardsPage } from './pages/VineyardsPage'
import { WinesPage } from './pages/WinesPage'

function App() {
  return (
    <Router>
      <div className="flex min-h-screen flex-col bg-[#080707] text-[#7a1f2b]">
        <Navbar />
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/vineyards" element={<VineyardsPage />} />
          <Route path="/wines" element={<WinesPage />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App

