import { useEffect, useState } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import api from '../lib/api'
import { formatDateTime, getEstadoPedidoClass, cn } from '../lib/utils'
import { Plus, Edit2, X, Loader2 } from 'lucide-react'

// ─── SERVICIOS ────────────────────────────────────────────────
export function ServiciosPage() {
  const [servicios, setServicios] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)
  const load = async () => { setLoading(true); const r = await api.get('/servicios'); setServicios(r.data); setLoading(false) }
  useEffect(() => { load() }, [])
  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div><h1 className="text-2xl font-bold">Servicios Hospitalarios</h1></div>
        <button onClick={() => { setEditing(null); setShowModal(true) }} className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90"><Plus size={16} /> Nuevo Servicio</button>
      </div>
      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {loading ? <div className="col-span-3 text-center py-10"><Loader2 className="animate-spin inline" size={20} /></div>
          : servicios.map(s => (
            <div key={s.id} className="bg-card border rounded-xl p-4 flex items-start justify-between">
              <div>
                <p className="font-semibold">{s.nombre}</p>
                {s.descripcion && <p className="text-sm text-muted-foreground mt-0.5">{s.descripcion}</p>}
                <span className={cn('inline-flex mt-2 px-2 py-0.5 rounded-full text-xs', s.activo ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-500')}>{s.activo ? 'Activo' : 'Inactivo'}</span>
              </div>
              <button onClick={() => { setEditing(s); setShowModal(true) }} className="p-1.5 hover:bg-muted rounded-lg text-muted-foreground"><Edit2 size={14} /></button>
            </div>
          ))}
      </div>
      {showModal && <ServicioModal servicio={editing} onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load() }} />}
    </div>
  )
}

function ServicioModal({ servicio, onClose, onSaved }) {
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({ defaultValues: servicio || {} })
  const onSubmit = async (data) => { if (servicio) await api.put('/servicios/' + servicio.id, data); else await api.post('/servicios', data); onSaved() }
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-sm animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b"><h2 className="font-semibold">{servicio ? 'Editar' : 'Nuevo'} Servicio</h2><button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button></div>
        <form onSubmit={handleSubmit(onSubmit)} className="p-5 space-y-4">
          <div><label className="block text-sm font-medium mb-1.5">Nombre *</label><input {...register('nombre', { required: true })} className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" /></div>
          <div><label className="block text-sm font-medium mb-1.5">Descripción</label><textarea {...register('descripcion')} rows={2} className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 resize-none" /></div>
          {servicio && <div className="flex items-center gap-2"><input type="checkbox" {...register('activo')} id="activo" className="w-4 h-4" /><label htmlFor="activo" className="text-sm">Servicio activo</label></div>}
          <div className="flex gap-3"><button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted">Cancelar</button><button type="submit" disabled={isSubmitting} className="flex-1 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 disabled:opacity-50 flex items-center justify-center gap-2">{isSubmitting && <Loader2 size={14} className="animate-spin" />} Guardar</button></div>
        </form>
      </div>
    </div>
  )
}

// ─── BITÁCORA ────────────────────────────────────────────────
export function BitacoraPage() {
  const [movimientos, setMovimientos] = useState([])
  const [loading, setLoading] = useState(true)
  const [tipo, setTipo] = useState('')
  const load = async () => { setLoading(true); const params = new URLSearchParams(); if (tipo) params.set('tipo', tipo); const r = await api.get('/bitacora?' + params + '&limit=100'); setMovimientos(r.data.data); setLoading(false) }
  useEffect(() => { load() }, [tipo])
  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div><h1 className="text-2xl font-bold">Bitácora de Movimientos</h1></div>
        <select value={tipo} onChange={e => setTipo(e.target.value)} className="px-3 py-2 border rounded-lg text-sm bg-background focus:outline-none">
          <option value="">Todos</option><option value="entrada">Entradas</option><option value="salida">Salidas</option>
        </select>
      </div>
      <div className="bg-card border rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="border-b bg-muted/40">{['Tipo','Insumo','Cantidad','Antes','Después','Usuario','Fecha'].map(h => <th key={h} className="text-left p-3 font-medium text-muted-foreground">{h}</th>)}</tr></thead>
            <tbody>
              {loading ? <tr><td colSpan={7} className="text-center py-10"><Loader2 className="animate-spin inline" size={16} /></td></tr>
                : movimientos.map(m => (
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

// ─── PEDIDOS ALMACÉN GENERAL ─────────────────────────────────
export function PedidosAlmacenPage() {
  const [pedidos, setPedidos] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const load = async () => { setLoading(true); const r = await api.get('/pedidos-almacen'); setPedidos(r.data.data); setLoading(false) }
  useEffect(() => { load() }, [])
  const cambiarEstado = async (id, estado) => { await api.put('/pedidos-almacen/' + id + '/estado', { estado }); load() }
  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div><h1 className="text-2xl font-bold">Pedidos al Almacén General</h1><p className="text-muted-foreground text-sm">Solicitudes de abastecimiento</p></div>
        <button onClick={() => setShowModal(true)} className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90"><Plus size={16} /> Nuevo Pedido</button>
      </div>
      <div className="bg-card border rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead><tr className="border-b bg-muted/40">{['#','Fecha','Solicitante','Almacén','Estado','Cambiar Estado'].map(h => <th key={h} className="text-left p-3 font-medium text-muted-foreground">{h}</th>)}</tr></thead>
            <tbody>
              {loading ? <tr><td colSpan={6} className="text-center py-10"><Loader2 className="animate-spin inline" size={16} /></td></tr>
                : pedidos.map(p => (
                  <tr key={p.id} className="border-b hover:bg-muted/20">
                    <td className="p-3 font-mono text-muted-foreground">#{p.id}</td>
                    <td className="p-3">{formatDateTime(p.fecha_pedido)}</td>
                    <td className="p-3">{p.usuario_nombre}</td>
                    <td className="p-3 text-muted-foreground">{p.almacen_nombre || '—'}</td>
                    <td className="p-3"><span className={cn('px-2 py-0.5 rounded-full text-xs font-medium', getEstadoPedidoClass(p.estado))}>{p.estado}</span></td>
                    <td className="p-3">
                      <select value={p.estado} onChange={e => cambiarEstado(p.id, e.target.value)} className="px-2 py-1 border rounded text-xs bg-background focus:outline-none">
                        <option value="pendiente">Pendiente</option><option value="enviado">Enviado</option><option value="recibido">Recibido</option><option value="cancelado">Cancelado</option>
                      </select>
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
        </div>
      </div>
      {showModal && <NuevoPedidoAlmacenModal onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load() }} />}
    </div>
  )
}

function NuevoPedidoAlmacenModal({ onClose, onSaved }) {
  const [insumos, setInsumos] = useState([])
  const { register, handleSubmit, control, formState: { isSubmitting } } = useForm({ defaultValues: { detalles: [{ insumo_id: '', cantidad: 1 }] } })
  const { fields, append, remove } = useFieldArray({ control, name: 'detalles' })
  useEffect(() => { api.get('/insumos?limit=100').then(r => setInsumos(r.data.data)) }, [])
  const onSubmit = async (data) => { await api.post('/pedidos-almacen', data); onSaved() }
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-lg max-h-[90vh] flex flex-col animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b shrink-0"><h2 className="font-semibold">Pedido al Almacén General</h2><button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button></div>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col flex-1 overflow-hidden">
          <div className="p-5 overflow-y-auto flex-1 space-y-4">
            <div><label className="block text-sm font-medium mb-1.5">Observaciones</label><input {...register('observaciones')} className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" /></div>
            <div>
              <div className="flex items-center justify-between mb-2"><label className="text-sm font-medium">Insumos *</label><button type="button" onClick={() => append({ insumo_id: '', cantidad: 1 })} className="text-xs text-primary hover:underline flex items-center gap-1"><Plus size={12} /> Agregar</button></div>
              <div className="space-y-2">
                {fields.map((field, idx) => (
                  <div key={field.id} className="flex gap-2 items-center">
                    <select {...register('detalles.' + idx + '.insumo_id', { required: true })} className="flex-1 px-3 py-1.5 border rounded-lg text-sm bg-background focus:outline-none focus:ring-2 focus:ring-primary/30">
                      <option value="">Seleccionar insumo</option>
                      {insumos.map(i => <option key={i.id} value={i.id}>{i.nombre}</option>)}
                    </select>
                    <input {...register('detalles.' + idx + '.cantidad', { required: true, min: 1 })} type="number" min="1" placeholder="Cant." className="w-24 px-3 py-1.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
                    {fields.length > 1 && <button type="button" onClick={() => remove(idx)} className="p-1.5 text-muted-foreground hover:text-red-500"><X size={14} /></button>}
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="p-5 border-t flex gap-3 shrink-0">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted">Cancelar</button>
            <button type="submit" disabled={isSubmitting} className="flex-1 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 disabled:opacity-50 flex items-center justify-center gap-2">{isSubmitting && <Loader2 size={14} className="animate-spin" />} Enviar Pedido</button>
          </div>
        </form>
      </div>
    </div>
  )
}
