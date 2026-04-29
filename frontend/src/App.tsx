import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Navbar } from './components/Navbar'
import { LoginPage } from './pages/LoginPage'
import { RegisterPage } from './pages/RegisterPage'
import { WinesPage } from './pages/WinesPage'

function App() {
  return (
    <Router>
      <div className="flex flex-col min-h-screen">
        <Navbar />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/wines" element={<WinesPage />} />
          <Route path="/" element={<Navigate to="/wines" replace />} />
          <Route path="*" element={<Navigate to="/wines" replace />} />
        </Routes>
      </div>
    </Router>
  )
}

export default App

