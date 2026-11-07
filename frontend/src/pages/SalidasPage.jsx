// SalidasPage.jsx
import { useEffect, useState } from 'react'
import { useForm, useFieldArray } from 'react-hook-form'
import api from '../lib/api'
import { formatDateTime } from '../lib/utils'
import { Plus, Trash2, X, Loader2, ShoppingBag } from 'lucide-react'

export function SalidasPage() {
  const [salidas, setSalidas] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)

  const load = async () => {
    setLoading(true)
    const r = await api.get('/salidas')
    setSalidas(r.data.data)
    setLoading(false)
  }

  useEffect(() => { load() }, [])

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Salidas a Servicios</h1>
          <p className="text-muted-foreground text-sm">Distribución de insumos a servicios hospitalarios</p>
        </div>
        <button onClick={() => setShowModal(true)}
          className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 transition-colors">
          <Plus size={16} /> Nueva Salida
        </button>
      </div>

      <div className="bg-card border rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b bg-muted/40">
                <th className="text-left p-3 font-medium text-muted-foreground">#</th>
                <th className="text-left p-3 font-medium text-muted-foreground">Fecha</th>
                <th className="text-left p-3 font-medium text-muted-foreground">Registrado por</th>
                <th className="text-left p-3 font-medium text-muted-foreground">Servicio destino</th>
                <th className="text-right p-3 font-medium text-muted-foreground">Items</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={5} className="text-center py-10 text-muted-foreground">
                  <Loader2 className="animate-spin inline mr-2" size={16} />Cargando...
                </td></tr>
              ) : salidas.length === 0 ? (
                <tr><td colSpan={5} className="text-center py-12 text-muted-foreground">
                  <ShoppingBag size={32} className="mx-auto mb-2 opacity-30" />
                  Sin salidas registradas
                </td></tr>
              ) : salidas.map(s => (
                <tr key={s.id} className="border-b hover:bg-muted/20">
                  <td className="p-3 font-mono text-muted-foreground">#{s.id}</td>
                  <td className="p-3">{formatDateTime(s.fecha_registro)}</td>
                  <td className="p-3">{s.usuario_nombre}</td>
                  <td className="p-3 text-muted-foreground">{s.servicio_nombre}</td>
                  <td className="p-3 text-right">{s.total_items}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && <SalidaModal onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load() }} />}
    </div>
  )
}

function SalidaModal({ onClose, onSaved }) {
  const [insumos, setInsumos] = useState([])
  const [servicios, setServicios] = useState([])
  const [error, setError] = useState('')
  const { register, handleSubmit, control, formState: { isSubmitting } } = useForm({
    defaultValues: { servicio_id: '', observaciones: '', detalles: [{ insumo_id: '', cantidad: 1 }] }
  })
  const { fields, append, remove } = useFieldArray({ control, name: 'detalles' })

  useEffect(() => {
    api.get('/insumos?limit=100').then(r => setInsumos(r.data.data))
    api.get('/servicios').then(r => setServicios(r.data))
  }, [])

  const onSubmit = async (data) => {
    setError('')
    try {
      await api.post('/salidas', data)
      onSaved()
    } catch (err) {
      setError(err.response?.data?.error || 'Error al registrar la salida')
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-2xl max-h-[90vh] flex flex-col animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b shrink-0">
          <h2 className="font-semibold">Registrar Salida a Servicio</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col flex-1 overflow-hidden">
          <div className="p-5 overflow-y-auto space-y-4 flex-1">
            {error && <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-2.5">{error}</p>}
            <div>
              <label className="block text-sm font-medium mb-1.5">Servicio destino *</label>
              <select {...register('servicio_id', { required: true })}
                className="w-full px-3 py-2 border rounded-lg text-sm bg-background focus:outline-none focus:ring-2 focus:ring-primary/30">
                <option value="">Seleccionar servicio</option>
                {servicios.map(s => <option key={s.id} value={s.id}>{s.nombre}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium mb-1.5">Observaciones</label>
              <input {...register('observaciones')}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
            <div>
              <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-medium">Insumos *</label>
                <button type="button" onClick={() => append({ insumo_id: '', cantidad: 1 })}
                  className="text-xs text-primary hover:underline flex items-center gap-1">
                  <Plus size={12} /> Agregar insumo
                </button>
              </div>
              <div className="space-y-2">
                {fields.map((field, idx) => (
                  <div key={field.id} className="flex gap-2 items-center p-3 bg-muted/30 rounded-lg">
                    <select {...register(`detalles.${idx}.insumo_id`, { required: true })}
                      className="flex-1 px-3 py-1.5 border rounded-lg text-sm bg-background focus:outline-none focus:ring-2 focus:ring-primary/30">
                      <option value="">Seleccionar insumo</option>
                      {insumos.map(i => <option key={i.id} value={i.id}>{i.nombre} ({parseFloat(i.existencia).toLocaleString()} disp.)</option>)}
                    </select>
                    <input {...register(`detalles.${idx}.cantidad`, { required: true, min: 0.01 })}
                      type="number" step="0.01" min="0.01" placeholder="Cantidad"
                      className="w-28 px-3 py-1.5 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
                    {fields.length > 1 && (
                      <button type="button" onClick={() => remove(idx)} className="p-1.5 hover:text-red-500 text-muted-foreground">
                        <Trash2 size={14} />
                      </button>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>
          <div className="p-5 border-t flex gap-3 shrink-0">
            <button type="button" onClick={onClose}
              className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted transition-colors">Cancelar</button>
            <button type="submit" disabled={isSubmitting}
              className="flex-1 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 disabled:opacity-50 flex items-center justify-center gap-2">
              {isSubmitting && <Loader2 size={14} className="animate-spin" />}
              Registrar Salida
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default SalidasPage
