import { useEffect, useState, useCallback } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import api from '../lib/api'
import { formatDateTime, getEstadoPedidoClass, cn } from '../lib/utils'
import { useAuthStore } from '../store/authStore'
import { Plus, Eye, CheckCircle, X, Loader2, ShoppingCart, Trash2 } from 'lucide-react'

export default function PedidosSubalmacenPage() {
  const { user } = useAuthStore()
  const [pedidos, setPedidos] = useState([])
  const [loading, setLoading] = useState(true)
  const [estadoFiltro, setEstadoFiltro] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [detailModal, setDetailModal] = useState(null)
  const [atenderModal, setAtenderModal] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    const params = new URLSearchParams()
    if (estadoFiltro) params.set('estado', estadoFiltro)
    const r = await api.get(`/pedidos-subalmacen?${params}`)
    setPedidos(r.data.data)
    setLoading(false)
  }, [estadoFiltro])

  useEffect(() => { load() }, [load])

  const cancelar = async (id) => {
    if (!confirm('¿Cancelar este pedido?')) return
    await api.put(`/pedidos-subalmacen/${id}/cancelar`)
    load()
  }

  const canCreate = ['Administrador', 'Responsable de Servicio'].includes(user?.rol)
  const canAtender = ['Administrador', 'Suministrador'].includes(user?.rol)

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Pedidos al Sub-Almacén</h1>
          <p className="text-muted-foreground text-sm">Gestión de solicitudes de insumos por servicio</p>
        </div>
        {canCreate && (
          <button onClick={() => setShowModal(true)}
            className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 transition-colors">
            <Plus size={16} /> Nuevo Pedido
          </button>
        )}
      </div>

      <div className="flex gap-3">
        {['', 'pendiente', 'atendido', 'cancelado'].map(e => (
          <button key={e}
            onClick={() => setEstadoFiltro(e)}
            className={cn('px-4 py-1.5 rounded-lg text-sm transition-colors border',
              estadoFiltro === e ? 'bg-primary text-primary-foreground border-primary' : 'hover:bg-muted border-transparent')}>
            {e === '' ? 'Todos' : e.charAt(0).toUpperCase() + e.slice(1)}
          </button>
        ))}
      </div>

      <div className="bg-card border rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-muted/40">
              <th className="text-left p-3 font-medium text-muted-foreground">#</th>
              <th className="text-left p-3 font-medium text-muted-foreground">Servicio</th>
              <th className="text-left p-3 font-medium text-muted-foreground">Solicitante</th>
              <th className="text-left p-3 font-medium text-muted-foreground">Fecha</th>
              <th className="text-left p-3 font-medium text-muted-foreground">Estado</th>
              <th className="p-3 text-right font-medium text-muted-foreground">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} className="text-center py-10 text-muted-foreground">
                <Loader2 className="animate-spin inline mr-2" size={16} />Cargando...
              </td></tr>
            ) : pedidos.length === 0 ? (
              <tr><td colSpan={6} className="text-center py-10 text-muted-foreground">
                <ShoppingCart size={32} className="mx-auto mb-2 opacity-30" />
                Sin pedidos
              </td></tr>
            ) : pedidos.map(p => (
              <tr key={p.id} className="border-b hover:bg-muted/20">
                <td className="p-3 font-mono text-muted-foreground">#{p.id}</td>
                <td className="p-3 font-medium">{p.servicio_nombre}</td>
                <td className="p-3 text-muted-foreground">{p.solicitante_nombre}</td>
                <td className="p-3 text-muted-foreground">{formatDateTime(p.fecha_pedido)}</td>
                <td className="p-3">
                  <span className={cn('inline-flex px-2 py-0.5 rounded-full text-xs font-medium', getEstadoPedidoClass(p.estado))}>
                    {p.estado}
                  </span>
                </td>
                <td className="p-3">
                  <div className="flex justify-end gap-1">
                    <button onClick={() => setDetailModal(p.id)}
                      className="p-1.5 hover:bg-muted rounded-lg text-muted-foreground hover:text-foreground">
                      <Eye size={14} />
                    </button>
                    {canAtender && p.estado === 'pendiente' && (
                      <button onClick={() => setAtenderModal(p.id)}
                        className="p-1.5 hover:bg-emerald-50 rounded-lg text-muted-foreground hover:text-emerald-600">
                        <CheckCircle size={14} />
                      </button>
                    )}
                    {p.estado === 'pendiente' && (
                      <button onClick={() => cancelar(p.id)}
                        className="p-1.5 hover:bg-red-50 rounded-lg text-muted-foreground hover:text-red-600">
                        <X size={14} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && <NuevoPedidoModal onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load() }} />}
      {detailModal && <DetallePedidoModal id={detailModal} onClose={() => setDetailModal(null)} />}
      {atenderModal && <AtenderPedidoModal id={atenderModal} onClose={() => setAtenderModal(null)} onSaved={() => { setAtenderModal(null); load() }} />}
    </div>
  )
}

function NuevoPedidoModal({ onClose, onSaved }) {
  const [servicios, setServicios] = useState([])
  const [insumos, setInsumos] = useState([])
  const { register, handleSubmit, control, formState: { isSubmitting } } = useForm({
    defaultValues: { detalles: [{ insumo_id: '', cantidad: 1 }] }
  })
  const { fields, append, remove } = useFieldArray({ control, name: 'detalles' })

  useEffect(() => {
    Promise.all([api.get('/servicios'), api.get('/insumos?limit=100')])
      .then(([s, i]) => { setServicios(s.data); setInsumos(i.data.data) })
  }, [])

  const onSubmit = async (data) => { await api.post('/pedidos-subalmacen', data); onSaved() }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-lg max-h-[90vh] flex flex-col animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b shrink-0">
          <h2 className="font-semibold">Nuevo Pedido al Sub-Almacén</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col flex-1 overflow-hidden">
          <div className="p-5 overflow-y-auto space-y-4 flex-1">
            <div>
              <label className="block text-sm font-medium mb-1.5">Servicio *</label>
              <select {...register('servicio_id', { required: true })}
                className="w-full px-3 py-2 border rounded-lg text-sm bg-background focus:outline-none focus:ring-2 focus:ring-primary/30">
                <option value="">Seleccionar servicio</option>
                {servicios.map(s => <option key={s.id} value={s.id}>{s.nombre}</option>)}
              </select>
            </div>
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-medium">Insumos *</label>
                <button type="button" onClick={() => append({ insumo_id: '', cantidad: 1 })}
                  className="text-xs text-primary hover:underline flex items-center gap-1">
                  <Plus size={12} /> Agregar
                </button>
              </div>
              <div className="space-y-2">
                {fields.map((field, idx) => (
                  <div key={field.id} className="flex gap-2 items-center">
                    <select {...register(`detalles.${idx}.insumo_id`, { required: true })}
                      className="flex-1 px-3 py-1.5 border rounded-lg text-sm bg-background focus:outline-none focus:ring-2 focus:ring-primary/30">
                      <option value="">Seleccionar insumo</option>
                      {insumos.map(i => <option key={i.id} value={i.id}>{i.nombre} (existe: {parseFloat(i.existencia).toLocaleString()})</option>)}
                    </select>
                    <input {...register(`detalles.${idx}.cantidad`, { required: true, min: 0.01 })}
                      type="number" step="0.01" min="0.01" placeholder="Cant."
                      className="w-24 px-3 py-1.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
                    {fields.length > 1 && (
                      <button type="button" onClick={() => remove(idx)} className="p-1.5 text-muted-foreground hover:text-red-500">
                        <Trash2 size={14} />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium mb-1.5">Observaciones</label>
              <textarea {...register('observaciones')} rows={2}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 resize-none" />
            </div>
          </div>
          <div className="p-5 border-t flex gap-3 shrink-0">
            <button type="button" onClick={onClose}
              className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted">Cancelar</button>
            <button type="submit" disabled={isSubmitting}
              className="flex-1 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 disabled:opacity-50 flex items-center justify-center gap-2">
              {isSubmitting && <Loader2 size={14} className="animate-spin" />} Enviar Pedido
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

function DetallePedidoModal({ id, onClose }) {
  const [data, setData] = useState(null)
  useEffect(() => { api.get(`/pedidos-subalmacen/${id}`).then(r => setData(r.data)) }, [id])

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-md animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-semibold">Detalle Pedido #{id}</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <div className="p-5 space-y-3">
          {!data ? <div className="text-center py-6"><Loader2 className="animate-spin inline" size={20} /></div> : (
            <>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div><span className="text-muted-foreground">Servicio:</span> <strong>{data.servicio_nombre}</strong></div>
                <div><span className="text-muted-foreground">Estado:</span> <span className={cn('px-2 py-0.5 rounded-full text-xs', getEstadoPedidoClass(data.estado))}>{data.estado}</span></div>
              </div>
              <div>
                <p className="text-sm font-medium mb-2">Insumos solicitados:</p>
                <div className="divide-y border rounded-lg overflow-hidden">
                  {data.detalles?.map(d => (
                    <div key={d.id} className="flex items-center justify-between px-3 py-2 text-sm">
                      <span>{d.insumo_nombre}</span>
                      <span className="text-muted-foreground">{d.cantidad_solicitada} / {d.cantidad_surtida} {d.unidad_medida}</span>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}

function AtenderPedidoModal({ id, onClose, onSaved }) {
  const [data, setData] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => { api.get(`/pedidos-subalmacen/${id}`).then(r => setData(r.data)) }, [id])

  const atender = async () => {
    setSubmitting(true)
    try {
      await api.put(`/pedidos-subalmacen/${id}/atender`, {})
      onSaved()
    } finally { setSubmitting(false) }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-md animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-semibold">Atender Pedido #{id}</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <div className="p-5 space-y-4">
          {!data ? <div className="text-center py-6"><Loader2 className="animate-spin inline" size={20} /></div> : (
            <>
              <p className="text-sm text-muted-foreground">Se distribuirán los siguientes insumos al servicio <strong>{data.servicio_nombre}</strong>:</p>
              <div className="divide-y border rounded-lg overflow-hidden">
                {data.detalles?.map(d => (
                  <div key={d.id} className="flex items-center justify-between px-3 py-2 text-sm">
                    <span>{d.insumo_nombre}</span>
                    <div className="text-right">
                      <p className="font-medium">{d.cantidad_solicitada} {d.unidad_medida}</p>
                      <p className="text-xs text-muted-foreground">En stock: {parseFloat(d.existencia).toLocaleString()}</p>
                    </div>
                  </div>
                ))}
              </div>
              <div className="flex gap-3">
                <button onClick={onClose} className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted">Cancelar</button>
                <button onClick={atender} disabled={submitting}
                  className="flex-1 py-2 bg-emerald-600 text-white rounded-lg text-sm font-medium hover:bg-emerald-700 disabled:opacity-50 flex items-center justify-center gap-2">
                  {submitting && <Loader2 size={14} className="animate-spin" />}
                  Confirmar Surtido
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
