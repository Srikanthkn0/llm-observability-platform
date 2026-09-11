import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import { useAuth } from './contexts/AuthContext'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import TracesPage from './pages/TracesPage'
import TraceDetailPage from './pages/TraceDetailPage'
import EvalsPage from './pages/EvalsPage'
import GuardrailsPage from './pages/GuardrailsPage'
import CostsPage from './pages/CostsPage'
import AlertsPage from './pages/AlertsPage'
import SettingsPage from './pages/SettingsPage'

function Private({ children }: { children: React.ReactNode }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return <>{children}</>
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<Private><Layout /></Private>}>
        <Route index element={<DashboardPage />} />
        <Route path="traces" element={<TracesPage />} />
        <Route path="traces/:traceId" element={<TraceDetailPage />} />
        <Route path="evals" element={<EvalsPage />} />
        <Route path="guardrails" element={<GuardrailsPage />} />
        <Route path="costs" element={<CostsPage />} />
        <Route path="alerts" element={<AlertsPage />} />
        <Route path="settings" element={<SettingsPage />} />
      </Route>
    </Routes>
  )
}
