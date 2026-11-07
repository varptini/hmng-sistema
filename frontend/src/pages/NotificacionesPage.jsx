// NotificacionesPage.jsx
import { useEffect, useState } from 'react'
import api from '../lib/api'
import { formatRelative, cn } from '../lib/utils'
import { Loader2, Bell, CheckCheck, AlertTriangle, PackageX, ClipboardList, PackageCheck } from 'lucide-react'

const ICONS = {
  caducidad: AlertTriangle,
  stock_minimo: PackageX,
  pedido_nuevo: ClipboardList,
  pedido_atendido: PackageCheck,
}

export function NotificacionesPage() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)

  const load = async () => {
    setLoading(true)
    const r = await api.get('/notificaciones')
    setData(r.data.data)
    setLoading(false)
  }

  useEffect(() => { load() }, [])

  const marcarLeidas = async () => {
    await api.put('/notificaciones/marcar-leidas')
    load()
  }

  const hayNoLeidas = data.some(n => !n.leida)

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Notificaciones</h1>
          <p className="text-muted-foreground text-sm">Alertas de stock, caducidad y pedidos</p>
        </div>
        <button onClick={marcarLeidas} disabled={!hayNoLeidas}
          className="flex items-center gap-2 px-4 py-2 border rounded-lg text-sm font-medium hover:bg-muted disabled:opacity-50 transition-colors">
          <CheckCheck size={16} /> Marcar todas como leídas
        </button>
      </div>

      <div className="bg-card border rounded-xl divide-y">
        {loading ? (
          <div className="text-center py-10 text-muted-foreground"><Loader2 className="animate-spin inline mr-2" size={16} />Cargando...</div>
        ) : data.length === 0 ? (
          <div className="text-center py-12 text-muted-foreground">
            <Bell size={32} className="mx-auto mb-2 opacity-30" />
            Sin notificaciones
          </div>
        ) : data.map(n => {
          const Icon = ICONS[n.tipo] || Bell
          return (
            <div key={n.id} className={cn('flex items-start gap-3 p-4', !n.leida && 'bg-primary/5')}>
              <div className={cn('p-2 rounded-lg shrink-0', n.leida ? 'bg-muted text-muted-foreground' : 'bg-primary/10 text-primary')}>
                <Icon size={16} />
              </div>
              <div className="flex-1 min-w-0">
                <p className={cn('text-sm', !n.leida && 'font-medium')}>{n.mensaje}</p>
                <p className="text-xs text-muted-foreground mt-0.5">{formatRelative(n.created_at)}</p>
              </div>
              {!n.leida && <span className="w-2 h-2 rounded-full bg-primary mt-2 shrink-0" />}
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default NotificacionesPage
