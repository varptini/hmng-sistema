import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/Layout'
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'
import InsumosPage from './pages/InsumosPage'
import EntradasPage from './pages/EntradasPage'
import PedidosSubalmacenPage from './pages/PedidosSubalmacenPage'
import PedidosAlmacenPage from './pages/PedidosAlmacenPage'
import UsuariosPage from './pages/UsuariosPage'
import EmpleadosPage from './pages/EmpleadosPage'
import ServiciosPage from './pages/ServiciosPage'
import BitacoraPage from './pages/BitacoraPage'
import { Toaster } from './components/ui/toaster'

function ProtectedRoute({ children, roles }) {
  const { token, user } = useAuthStore()
  if (!token) return <Navigate to="/login" replace />
  if (roles && !roles.includes(user?.rol)) return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={
          <ProtectedRoute><Layout /></ProtectedRoute>
        }>
          <Route index element={<DashboardPage />} />
          <Route path="insumos" element={<InsumosPage />} />
          <Route path="entradas" element={
            <ProtectedRoute roles={['Administrador','Abastecedor']}>
              <EntradasPage />
            </ProtectedRoute>
          } />
          <Route path="pedidos/subalmacen" element={<PedidosSubalmacenPage />} />
          <Route path="pedidos/almacen" element={
            <ProtectedRoute roles={['Administrador','Abastecedor']}>
              <PedidosAlmacenPage />
            </ProtectedRoute>
          } />
          <Route path="usuarios" element={
            <ProtectedRoute roles={['Administrador']}>
              <UsuariosPage />
            </ProtectedRoute>
          } />
          <Route path="empleados" element={
            <ProtectedRoute roles={['Administrador']}>
              <EmpleadosPage />
            </ProtectedRoute>
          } />
          <Route path="servicios" element={
            <ProtectedRoute roles={['Administrador']}>
              <ServiciosPage />
            </ProtectedRoute>
          } />
          <Route path="bitacora" element={<BitacoraPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
      <Toaster />
    </BrowserRouter>
  )
}
