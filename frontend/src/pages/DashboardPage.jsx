import { useEffect, useState } from 'react'
import api from '../lib/api'
import { formatDateTime, formatNumber } from '../lib/utils'
import {
  Package, TrendingUp, TrendingDown, AlertTriangle, Clock,
  ShoppingCart, BarChart3, ArrowUpRight, ArrowDownRight
} from 'lucide-react'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, BarChart, Bar, Legend
} from 'recharts'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'

function StatCard({ icon: Icon, label, value, color, sub }) {
  return (
    <div className="stat-card flex items-start gap-4">
      <div className={`p-2.5 rounded-xl ${color}`}>
        <Icon size={20} className="text-white" />
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-2xl font-bold text-foreground">{value}</p>
        <p className="text-sm text-muted-foreground mt-0.5">{label}</p>
        {sub && <p className="text-xs text-muted-foreground/70 mt-0.5">{sub}</p>}
      </div>
    </div>
  )
}

export default function DashboardPage() {
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    api.get('/dashboard').then(r => { setStats(r.data); setLoading(false) }).catch(() => setLoading(false))
  }, [])

  if (loading) return (
    <div className="flex items-center justify-center h-64">
      <div className="animate-spin w-8 h-8 border-4 border-primary border-t-transparent rounded-full" />
    </div>
  )

  if (!stats) return <p className="text-muted-foreground">Error cargando datos.</p>

  const { resumen, stock_critico, movimientos_recientes, tendencia } = stats

  const tendenciaFmt = tendencia?.map(t => ({
    dia: format(new Date(t.dia), 'dd MMM', { locale: es }),
    Entradas: parseFloat(t.entradas),
    Salidas: parseFloat(t.salidas),
  })) || []

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground text-sm mt-0.5">Resumen general del sub-almacén</p>
      </div>

      {/* Stats grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={Package} label="Total Insumos" value={resumen.total_insumos}
          color="bg-blue-500" />
        <StatCard icon={AlertTriangle} label="Stock Bajo" value={resumen.stock_bajo}
          color={resumen.stock_bajo > 0 ? "bg-orange-500" : "bg-emerald-500"} />
        <StatCard icon={Clock} label="Por Caducar (30d)" value={resumen.por_caducar}
          color={resumen.por_caducar > 0 ? "bg-amber-500" : "bg-emerald-500"} />
        <StatCard icon={ShoppingCart} label="Pedidos Pendientes" value={resumen.pedidos_pendientes}
          color={resumen.pedidos_pendientes > 0 ? "bg-purple-500" : "bg-emerald-500"} />
        <StatCard icon={TrendingUp} label="Entradas del Mes" value={formatNumber(resumen.entradas_mes)}
          color="bg-teal-500" sub="unidades ingresadas" />
        <StatCard icon={TrendingDown} label="Salidas del Mes" value={formatNumber(resumen.salidas_mes)}
          color="bg-rose-500" sub="unidades distribuidas" />
        <StatCard icon={AlertTriangle} label="Caducados" value={resumen.caducados}
          color={resumen.caducados > 0 ? "bg-red-600" : "bg-emerald-500"} />
        <StatCard icon={BarChart3} label="Pedidos Almacén" value={resumen.pedidos_almacen_pendientes}
          color="bg-indigo-500" sub="pendientes/enviados" />
      </div>

      {/* Charts row */}
      <div className="grid lg:grid-cols-2 gap-6">
        {/* Tendencia */}
        <div className="bg-card border rounded-xl p-5">
          <h3 className="font-semibold mb-4">Movimientos — Últimos 7 días</h3>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={tendenciaFmt}>
              <defs>
                <linearGradient id="colorEnt" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.2}/>
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                </linearGradient>
                <linearGradient id="colorSal" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.2}/>
                  <stop offset="95%" stopColor="#f43f5e" stopOpacity={0}/>
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="dia" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip />
              <Legend />
              <Area type="monotone" dataKey="Entradas" stroke="#3b82f6" fill="url(#colorEnt)" strokeWidth={2} />
              <Area type="monotone" dataKey="Salidas" stroke="#f43f5e" fill="url(#colorSal)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        {/* Stock crítico */}
        <div className="bg-card border rounded-xl p-5">
          <h3 className="font-semibold mb-4">Insumos con Stock Crítico</h3>
          {stock_critico?.length === 0 ? (
            <div className="flex items-center justify-center h-44 text-muted-foreground text-sm">
              Sin alertas de stock
            </div>
          ) : (
            <div className="space-y-3">
              {stock_critico?.map(i => (
                <div key={i.id} className="flex items-center gap-3">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium truncate">{i.nombre}</p>
                    <p className="text-xs text-muted-foreground">{formatNumber(i.existencia, 1)} {i.unidad_medida} (mín: {formatNumber(i.cantidad_minima, 1)})</p>
                  </div>
                  <div className="text-right shrink-0">
                    <div className="w-16 bg-muted rounded-full h-1.5">
                      <div
                        className={`h-1.5 rounded-full ${parseFloat(i.porcentaje_stock) < 50 ? 'bg-red-500' : 'bg-amber-500'}`}
                        style={{ width: `${Math.min(100, Math.max(0, parseFloat(i.porcentaje_stock)))}%` }}
                      />
                    </div>
                    <p className="text-xs text-muted-foreground mt-0.5">{i.porcentaje_stock}%</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Movimientos recientes */}
      <div className="bg-card border rounded-xl p-5">
        <h3 className="font-semibold mb-4">Movimientos Recientes</h3>
        <div className="divide-y">
          {movimientos_recientes?.map(m => (
            <div key={m.id} className="py-3 flex items-center gap-3">
              <div className={`p-1.5 rounded-lg ${m.tipo === 'entrada' ? 'bg-teal-100' : 'bg-rose-100'}`}>
                {m.tipo === 'entrada'
                  ? <ArrowUpRight size={14} className="text-teal-600" />
                  : <ArrowDownRight size={14} className="text-rose-600" />}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium truncate">{m.insumo_nombre}</p>
                <p className="text-xs text-muted-foreground">{m.usuario_nombre}</p>
              </div>
              <div className="text-right shrink-0">
                <p className={`text-sm font-semibold ${m.tipo === 'entrada' ? 'text-teal-600' : 'text-rose-600'}`}>
                  {m.tipo === 'entrada' ? '+' : '-'}{formatNumber(m.cantidad, 1)} {m.unidad_medida}
                </p>
                <p className="text-xs text-muted-foreground">{formatDateTime(m.fecha)}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
