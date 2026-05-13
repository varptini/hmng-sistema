import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import api from '../lib/api'
import { formatDate, getEstadoCaducidadClass, getEstadoStockClass, cn } from '../lib/utils'
import { useAuthStore } from '../store/authStore'
import { Plus, Search, Edit2, Trash2, X, Loader2, AlertTriangle, Package } from 'lucide-react'

const canEdit = (rol) => ['Administrador', 'Abastecedor'].includes(rol)
const canDelete = (rol) => rol === 'Administrador'

export default function InsumosPage() {
  const { user } = useAuthStore()
  const [insumos, setInsumos] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [search, setSearch] = useState('')
  const [alerta, setAlerta] = useState('')
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    try {
      const params = new URLSearchParams({ limit: 50 })
      if (search) params.set('search', search)
      if (alerta) params.set('alerta', alerta)
      const r = await api.get(`/insumos?${params}`)
      setInsumos(r.data.data)
      setTotal(r.data.total)
    } finally { setLoading(false) }
  }, [search, alerta])

  useEffect(() => {
    const t = setTimeout(load, search ? 400 : 0)
    return () => clearTimeout(t)
  }, [load])

  const handleDelete = async (id, nombre) => {
    if (!confirm(`¿Eliminar insumo "${nombre}"?`)) return
    await api.delete(`/insumos/${id}`)
    load()
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Insumos</h1>
          <p className="text-muted-foreground text-sm">{total} insumos registrados</p>
        </div>
        {canEdit(user?.rol) && (
          <button onClick={() => { setEditing(null); setShowModal(true) }}
            className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 transition-colors">
            <Plus size={16} /> Nuevo Insumo
          </button>
        )}
      </div>

      {/* Filters */}
      <div className="flex gap-3 flex-wrap">
        <div className="relative flex-1 min-w-48">
          <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <input
            value={search}
            onChange={e => setSearch(e.target.value)}
            placeholder="Buscar insumos..."
            className="w-full pl-9 pr-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
          />
        </div>
        <select
          value={alerta}
          onChange={e => setAlerta(e.target.value)}
          className="px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 bg-background"
        >
          <option value="">Todos</option>
          <option value="stock">Stock Bajo</option>
          <option value="caducidad">Por Caducar</option>
          <option value="caducado">Caducados</option>
        </select>
      </div>

      {/* Table */}
      <div className="bg-card border rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b bg-muted/40">
                <th className="text-left p-3 font-medium text-muted-foreground">Nombre</th>
                <th className="text-left p-3 font-medium text-muted-foreground">Unidad</th>
                <th className="text-right p-3 font-medium text-muted-foreground">Existencia</th>
                <th className="text-right p-3 font-medium text-muted-foreground">Mínimo</th>
                <th className="text-left p-3 font-medium text-muted-foreground">Caducidad</th>
                <th className="text-left p-3 font-medium text-muted-foreground">Estado</th>
                <th className="p-3 font-medium text-muted-foreground text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={7} className="text-center py-12 text-muted-foreground">
                  <Loader2 className="animate-spin inline mr-2" size={16} />Cargando...
                </td></tr>
              ) : insumos.length === 0 ? (
                <tr><td colSpan={7} className="text-center py-12 text-muted-foreground">
                  <Package size={32} className="mx-auto mb-2 opacity-30" />
                  Sin insumos registrados
                </td></tr>
              ) : insumos.map(ins => (
                <tr key={ins.id} className="border-b hover:bg-muted/20 transition-colors">
                  <td className="p-3">
                    <p className="font-medium">{ins.nombre}</p>
                    {ins.lote && <p className="text-xs text-muted-foreground">Lote: {ins.lote}</p>}
                  </td>
                  <td className="p-3 text-muted-foreground">{ins.unidad_medida}</td>
                  <td className="p-3 text-right font-mono font-medium">{parseFloat(ins.existencia).toLocaleString()}</td>
                  <td className="p-3 text-right font-mono text-muted-foreground">{parseFloat(ins.cantidad_minima).toLocaleString()}</td>
                  <td className="p-3">{formatDate(ins.fecha_caducidad)}</td>
                  <td className="p-3">
                    <div className="flex gap-1.5 flex-wrap">
                      <span className={cn('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', getEstadoCaducidadClass(ins.estado_caducidad))}>
                        {ins.estado_caducidad?.replace('_', ' ')}
                      </span>
                      <span className={cn('inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium border', getEstadoStockClass(ins.estado_stock))}>
                        {ins.estado_stock?.replace('_', ' ')}
                      </span>
                    </div>
                  </td>
                  <td className="p-3">
                    <div className="flex justify-end gap-1">
                      {canEdit(user?.rol) && (
                        <button onClick={() => { setEditing(ins); setShowModal(true) }}
                          className="p-1.5 hover:bg-muted rounded-lg transition-colors text-muted-foreground hover:text-foreground">
                          <Edit2 size={14} />
                        </button>
                      )}
                      {canDelete(user?.rol) && (
                        <button onClick={() => handleDelete(ins.id, ins.nombre)}
                          className="p-1.5 hover:bg-red-50 rounded-lg transition-colors text-muted-foreground hover:text-red-600">
                          <Trash2 size={14} />
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <InsumoModal
          insumo={editing}
          onClose={() => setShowModal(false)}
          onSaved={() => { setShowModal(false); load() }}
        />
      )}
    </div>
  )
}

function InsumoModal({ insumo, onClose, onSaved }) {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: insumo ? {
      nombre: insumo.nombre,
      descripcion: insumo.descripcion || '',
      unidad_medida: insumo.unidad_medida,
      cantidad_minima: insumo.cantidad_minima,
      lote: insumo.lote || '',
      fecha_caducidad: insumo.fecha_caducidad?.split('T')[0] || '',
      codigo_barras: insumo.codigo_barras || '',
      existencia: insumo.existencia,
    } : {}
  })

  const onSubmit = async (data) => {
    if (insumo) {
      await api.put(`/insumos/${insumo.id}`, data)
    } else {
      await api.post('/insumos', data)
    }
    onSaved()
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-md animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-semibold">{insumo ? 'Editar Insumo' : 'Nuevo Insumo'}</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="p-5 space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="block text-sm font-medium mb-1.5">Nombre *</label>
              <input {...register('nombre', { required: 'Requerido' })}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
              {errors.nombre && <p className="text-xs text-red-500 mt-1">{errors.nombre.message}</p>}
            </div>
            <div>
              <label className="block text-sm font-medium mb-1.5">Unidad de Medida *</label>
              <input {...register('unidad_medida', { required: 'Requerido' })}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                placeholder="Ej: Pieza, Caja x100" />
            </div>
            {!insumo && (
              <div>
                <label className="block text-sm font-medium mb-1.5">Existencia inicial</label>
                <input {...register('existencia')} type="number" step="0.01" min="0" defaultValue="0"
                  className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
              </div>
            )}
            <div>
              <label className="block text-sm font-medium mb-1.5">Cantidad Mínima</label>
              <input {...register('cantidad_minima')} type="number" step="0.01" min="0"
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1.5">Lote</label>
              <input {...register('lote')}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1.5">Fecha Caducidad</label>
              <input {...register('fecha_caducidad')} type="date"
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium mb-1.5">Código de Barras</label>
              <input {...register('codigo_barras')}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div className="col-span-2">
              <label className="block text-sm font-medium mb-1.5">Descripción</label>
              <textarea {...register('descripcion')} rows={2}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 resize-none" />
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose}
              className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted transition-colors">
              Cancelar
            </button>
            <button type="submit" disabled={isSubmitting}
              className="flex-1 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 disabled:opacity-50 transition-colors flex items-center justify-center gap-2">
              {isSubmitting && <Loader2 size={14} className="animate-spin" />}
              {insumo ? 'Guardar cambios' : 'Registrar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
