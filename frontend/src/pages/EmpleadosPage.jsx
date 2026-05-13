// EmpleadosPage
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import api from '../lib/api'
import { Plus, Edit2, X, Loader2 } from 'lucide-react'

export function EmpleadosPage() {
  const [empleados, setEmpleados] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)

  const load = async () => { setLoading(true); const r = await api.get('/empleados'); setEmpleados(r.data); setLoading(false) }
  useEffect(() => { load() }, [])

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Empleados</h1>
          <p className="text-muted-foreground text-sm">Personal registrado en el sistema</p>
        </div>
        <button onClick={() => { setEditing(null); setShowModal(true) }}
          className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90">
          <Plus size={16} /> Nuevo Empleado
        </button>
      </div>
      <div className="bg-card border rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-muted/40">
              {['Nombre','Correo','Teléfono','Celular',''].map(h => (
                <th key={h} className="text-left p-3 font-medium text-muted-foreground">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? <tr><td colSpan={5} className="text-center py-10 text-muted-foreground"><Loader2 className="animate-spin inline" size={16} /></td></tr>
            : empleados.map(e => (
              <tr key={e.id} className="border-b hover:bg-muted/20">
                <td className="p-3 font-medium">{e.nombre}</td>
                <td className="p-3 text-muted-foreground">{e.correo || '—'}</td>
                <td className="p-3 text-muted-foreground">{e.telefono || '—'}</td>
                <td className="p-3 text-muted-foreground">{e.celular || '—'}</td>
                <td className="p-3 text-right">
                  <button onClick={() => { setEditing(e); setShowModal(true) }}
                    className="p-1.5 hover:bg-muted rounded-lg text-muted-foreground hover:text-foreground">
                    <Edit2 size={14} />
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {showModal && <EmpleadoModal empleado={editing} onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load() }} />}
    </div>
  )
}

function EmpleadoModal({ empleado, onClose, onSaved }) {
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({ defaultValues: empleado || {} })
  const onSubmit = async (data) => {
    if (empleado) { await api.put(`/empleados/${empleado.id}`, data) } else { await api.post('/empleados', data) }
    onSaved()
  }
  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-md animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-semibold">{empleado ? 'Editar Empleado' : 'Nuevo Empleado'}</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="p-5 space-y-4">
          {[['nombre','Nombre completo *',true],['correo','Correo electrónico'],['telefono','Teléfono'],['celular','Celular'],['direccion','Dirección']].map(([f,l,req]) => (
            <div key={f}>
              <label className="block text-sm font-medium mb-1.5">{l}</label>
              <input {...register(f, req ? { required: 'Requerido' } : {})} type={f==='correo'?'email':'text'}
                className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            </div>
          ))}
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted">Cancelar</button>
            <button type="submit" disabled={isSubmitting}
              className="flex-1 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 disabled:opacity-50 flex items-center justify-center gap-2">
              {isSubmitting && <Loader2 size={14} className="animate-spin" />} Guardar
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default EmpleadosPage
