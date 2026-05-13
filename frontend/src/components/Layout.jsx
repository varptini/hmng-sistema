import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import { useAuthStore } from '../store/authStore'
import api from '../lib/api'
import { io } from 'socket.io-client'
import {
  LayoutDashboard, Package, TrendingUp, ShoppingCart, Warehouse,
  Users, UserCheck, Building2, ClipboardList, LogOut, Bell,
  Menu, X, ChevronRight, AlertTriangle
} from 'lucide-react'
import { cn } from '../lib/utils'

const navItems = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard', end: true, roles: null },
  { to: '/insumos', icon: Package, label: 'Insumos', roles: null },
  { to: '/entradas', icon: TrendingUp, label: 'Entradas', roles: ['Administrador','Abastecedor'] },
  { to: '/pedidos/subalmacen', icon: ShoppingCart, label: 'Pedidos Sub-Almacén', roles: null },
  { to: '/pedidos/almacen', icon: Warehouse, label: 'Pedidos Almacén Gral.', roles: ['Administrador','Abastecedor'] },
  { to: '/bitacora', icon: ClipboardList, label: 'Bitácora', roles: null },
  { to: '/servicios', icon: Building2, label: 'Servicios', roles: ['Administrador'] },
  { to: '/empleados', icon: UserCheck, label: 'Empleados', roles: ['Administrador'] },
  { to: '/usuarios', icon: Users, label: 'Usuarios', roles: ['Administrador'] },
]

export default function Layout() {
  const { user, token, logout } = useAuthStore()
  const navigate = useNavigate()
  const [sidebarOpen, setSidebarOpen] = useState(true)
  const [notifCount, setNotifCount] = useState(0)
  const [alertas, setAlertas] = useState({ por_caducar: 0, stock_bajo: 0 })

  useEffect(() => {
    // Notificaciones iniciales
    api.get('/notificaciones').then(r => setNotifCount(r.data.no_leidas)).catch(() => {})
    api.get('/insumos/alertas').then(r => setAlertas(r.data.totales)).catch(() => {})

    // Socket.io para notificaciones en tiempo real
    const socket = io(import.meta.env.VITE_API_URL?.replace('/api','') || 'http://localhost:4000', {
      auth: { token }
    })
    socket.on('notificacion', () => {
      setNotifCount(c => c + 1)
      api.get('/insumos/alertas').then(r => setAlertas(r.data.totales)).catch(() => {})
    })
    return () => socket.disconnect()
  }, [token])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const visibleNav = navItems.filter(item =>
    !item.roles || item.roles.includes(user?.rol)
  )

  const totalAlertas = alertas.por_caducar + alertas.stock_bajo

  return (
    <div className="flex h-screen bg-background overflow-hidden">
      {/* Sidebar */}
      <aside className={cn(
        'flex flex-col bg-sidebar text-sidebar-foreground transition-all duration-300 ease-in-out',
        sidebarOpen ? 'w-64' : 'w-16'
      )}>
        {/* Logo */}
        <div className="flex items-center justify-between p-4 border-b border-sidebar-border">
          {sidebarOpen && (
            <div className="animate-fade-in">
              <h1 className="text-base font-bold text-white leading-tight">HMNG</h1>
              <p className="text-xs text-sidebar-foreground/60">Gestión de Insumos</p>
            </div>
          )}
          <button
            onClick={() => setSidebarOpen(!sidebarOpen)}
            className="p-1.5 rounded-lg hover:bg-sidebar-accent/20 transition-colors ml-auto"
          >
            {sidebarOpen ? <X size={18} /> : <Menu size={18} />}
          </button>
        </div>

        {/* Nav */}
        <nav className="flex-1 p-2 space-y-0.5 overflow-y-auto">
          {visibleNav.map(({ to, icon: Icon, label, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) => cn(
                'flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all',
                isActive
                  ? 'bg-sidebar-accent text-white font-medium'
                  : 'text-sidebar-foreground/70 hover:bg-sidebar-accent/20 hover:text-white'
              )}
            >
              <Icon size={18} className="shrink-0" />
              {sidebarOpen && <span className="truncate animate-fade-in">{label}</span>}
            </NavLink>
          ))}
        </nav>

        {/* Alertas badge */}
        {totalAlertas > 0 && sidebarOpen && (
          <div className="mx-3 mb-2 p-3 bg-amber-500/20 border border-amber-500/30 rounded-lg">
            <div className="flex items-center gap-2 text-amber-300 text-xs">
              <AlertTriangle size={14} />
              <span className="font-medium">{totalAlertas} alertas activas</span>
            </div>
            <div className="mt-1 text-xs text-amber-300/70 space-y-0.5">
              {alertas.stock_bajo > 0 && <p>• {alertas.stock_bajo} insumos con stock bajo</p>}
              {alertas.por_caducar > 0 && <p>• {alertas.por_caducar} próximos a caducar</p>}
            </div>
          </div>
        )}

        {/* User footer */}
        <div className="p-3 border-t border-sidebar-border">
          <div className={cn('flex items-center gap-3', !sidebarOpen && 'justify-center')}>
            <div className="w-8 h-8 rounded-full bg-sidebar-accent flex items-center justify-center text-sm font-bold shrink-0">
              {user?.nombre?.[0]?.toUpperCase() || 'U'}
            </div>
            {sidebarOpen && (
              <div className="flex-1 min-w-0 animate-fade-in">
                <p className="text-sm font-medium truncate">{user?.nombre}</p>
                <p className="text-xs text-sidebar-foreground/50 truncate">{user?.rol}</p>
              </div>
            )}
            {sidebarOpen && (
              <button onClick={handleLogout} className="p-1 hover:text-red-400 transition-colors" title="Cerrar sesión">
                <LogOut size={16} />
              </button>
            )}
          </div>
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Topbar */}
        <header className="h-14 border-b bg-card flex items-center justify-between px-6 shrink-0">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <ChevronRight size={14} />
            <span>Hospital de la Madre y el Niño Guerrerense</span>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => {
                api.put('/notificaciones/marcar-leidas').then(() => setNotifCount(0))
              }}
              className="relative p-2 rounded-lg hover:bg-muted transition-colors"
            >
              <Bell size={18} />
              {notifCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 min-w-4 h-4 px-1 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                  {notifCount > 99 ? '99+' : notifCount}
                </span>
              )}
            </button>
          </div>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
