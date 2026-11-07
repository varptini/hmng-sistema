// ReportesPage.jsx
import { useEffect, useState } from 'react'
import api from '../lib/api'
import { formatDateTime, getEstadoCaducidadClass, getEstadoStockClass, cn } from '../lib/utils'
import { Loader2, Download, FileBarChart } from 'lucide-react'

function toCsv(rows, columns) {
  const header = columns.map(c => c.label).join(',')
  const body = rows.map(row => columns.map(c => {
    const value = c.value(row)
    const str = value == null ? '' : String(value)
    return /[",\n]/.test(str) ? `"${str.replace(/"/g, '""')}"` : str
  }).join(',')).join('\n')
  return `${header}\n${body}`
}

function downloadCsv(filename, csv) {
  const blob = new Blob([`﻿${csv}`], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}

export function ReportesPage() {
  const [tab, setTab] = useState('inventario')

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Reportes</h1>
          <p className="text-muted-foreground text-sm">Inventario actual y movimientos de insumos</p>
        </div>
      </div>

      <div className="flex gap-1 border-b">
        {[['inventario', 'Inventario'], ['movimientos', 'Movimientos']].map(([key, label]) => (
          <button key={key} onClick={() => setTab(key)}
            className={cn(
              'px-4 py-2 text-sm font-medium border-b-2 -mb-px transition-colors',
              tab === key ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'
            )}>
            {label}
          </button>
        ))}
      </div>

      {tab === 'inventario' ? <ReporteInventario /> : <ReporteMovimientos />}
    </div>
  )
}

function ReporteInventario() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api.get('/reportes/inventario').then(r => setData(r.data.data)).finally(() => setLoading(false))
  }, [])

  const exportar = () => {
    const csv = toCsv(data, [
      { label: 'Nombre', value: r => r.nombre },
      { label: 'Unidad', value: r => r.unidad_medida },
      { label: 'Existencia', value: r => r.existencia },
      { label: 'Minimo', value: r => r.cantidad_minima },
      { label: 'Lote', value: r => r.lote },
      { label: 'Caducidad', value: r => r.fecha_caducidad },
      { label: 'Estado stock', value: r => r.estado_stock },
      { label: 'Estado caducidad', value: r => r.estado_caducidad },
    ])
    downloadCsv('reporte_inventario.csv', csv)
  }

  return (
    <div className="space-y-4">
      <div className="flex justify-end">
        <button onClick={exportar} disabled={!data.length}
          className="flex items-center gap-2 px-3 py-1.5 border rounded-lg text-sm hover:bg-muted disabled:opacity-50">
          <Download size={14} /> Exportar CSV
        </button>
      </div>
      <div className="bg-card border rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b bg-muted/40">
                {['Nombre', 'Unidad', 'Existencia', 'Mínimo', 'Caducidad', 'Estado'].map(h => (
                  <th key={h} className="text-left p-3 font-medium text-muted-foreground">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} className="text-center py-10 text-muted-foreground"><Loader2 className="animate-spin inline mr-2" size={16} />Cargando...</td></tr>
              ) : data.length === 0 ? (
                <tr><td colSpan={6} className="text-center py-12 text-muted-foreground"><FileBarChart size={32} className="mx-auto mb-2 opacity-30" />Sin datos</td></tr>
              ) : data.map(i => (
                <tr key={i.id} className="border-b hover:bg-muted/20">
                  <td className="p-3 font-medium">{i.nombre}</td>
                  <td className="p-3 text-muted-foreground">{i.unidad_medida}</td>
                  <td className="p-3 font-mono">{parseFloat(i.existencia).toLocaleString()}</td>
                  <td className="p-3 font-mono text-muted-foreground">{parseFloat(i.cantidad_minima).toLocaleString()}</td>
                  <td className="p-3 text-muted-foreground">{i.fecha_caducidad ? new Date(i.fecha_caducidad).toLocaleDateString('es-MX') : '—'}</td>
                  <td className="p-3">
                    <div className="flex gap-1.5 flex-wrap">
                      <span className={cn('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', getEstadoStockClass(i.estado_stock))}>{i.estado_stock?.replace('_', ' ')}</span>
                      <span className={cn('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', getEstadoCaducidadClass(i.estado_caducidad))}>{i.estado_caducidad?.replace('_', ' ')}</span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

function ReporteMovimientos() {
  const [data, setData] = useState([])
  const [loading, setLoading] = useState(true)
  const [desde, setDesde] = useState('')
  const [hasta, setHasta] = useState('')

  const load = async () => {
    setLoading(true)
    const params = new URLSearchParams()
    if (desde && hasta) { params.set('desde', desde); params.set('hasta', hasta) }
    const r = await api.get(`/reportes/movimientos?${params}`)
    setData(r.data.data)
    setLoading(false)
  }

  useEffect(() => { load() }, []) // eslint-disable-line react-hooks/exhaustive-deps

  const exportar = () => {
    const csv = toCsv(data, [
      { label: 'Tipo', value: r => r.tipo },
      { label: 'Insumo', value: r => r.insumo_nombre },
      { label: 'Cantidad', value: r => r.cantidad },
      { label: 'Existencia anterior', value: r => r.existencia_anterior },
      { label: 'Existencia nueva', value: r => r.existencia_nueva },
      { label: 'Usuario', value: r => r.usuario_nombre },
      { label: 'Fecha', value: r => r.fecha },
    ])
    downloadCsv('reporte_movimientos.csv', csv)
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div className="flex items-center gap-2">
          <input type="date" value={desde} onChange={e => setDesde(e.target.value)}
            className="px-3 py-1.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          <span className="text-muted-foreground text-sm">a</span>
          <input type="date" value={hasta} onChange={e => setHasta(e.target.value)}
            className="px-3 py-1.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          <button onClick={load} className="px-3 py-1.5 border rounded-lg text-sm hover:bg-muted">Filtrar</button>
        </div>
        <button onClick={exportar} disabled={!data.length}
          className="flex items-center gap-2 px-3 py-1.5 border rounded-lg text-sm hover:bg-muted disabled:opacity-50">
          <Download size={14} /> Exportar CSV
        </button>
      </div>
      <div className="bg-card border rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b bg-muted/40">
                {['Tipo', 'Insumo', 'Cantidad', 'Antes', 'Después', 'Usuario', 'Fecha'].map(h => (
                  <th key={h} className="text-left p-3 font-medium text-muted-foreground">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7} className="text-center py-10 text-muted-foreground"><Loader2 className="animate-spin inline mr-2" size={16} />Cargando...</td></tr>
              ) : data.length === 0 ? (
                <tr><td colSpan={7} className="text-center py-12 text-muted-foreground"><FileBarChart size={32} className="mx-auto mb-2 opacity-30" />Sin movimientos</td></tr>
              ) : data.map(m => (
                <tr key={m.id} className="border-b hover:bg-muted/20">
                  <td className="p-3"><span className={cn('px-2 py-0.5 rounded-full text-xs font-medium', m.tipo === 'entrada' ? 'bg-teal-50 text-teal-700' : 'bg-rose-50 text-rose-700')}>{m.tipo}</span></td>
                  <td className="p-3 font-medium">{m.insumo_nombre}</td>
                  <td className="p-3 font-mono">{parseFloat(m.cantidad).toLocaleString()} {m.unidad_medida}</td>
                  <td className="p-3 font-mono text-muted-foreground">{parseFloat(m.existencia_anterior || 0).toLocaleString()}</td>
                  <td className="p-3 font-mono">{parseFloat(m.existencia_nueva || 0).toLocaleString()}</td>
                  <td className="p-3 text-muted-foreground">{m.usuario_nombre}</td>
                  <td className="p-3 text-muted-foreground">{formatDateTime(m.fecha)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}

export default ReportesPage
