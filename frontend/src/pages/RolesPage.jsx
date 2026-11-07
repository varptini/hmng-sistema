// RolesPage.jsx
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import api from '../lib/api'
import { Plus, Edit2, Trash2, X, Loader2, Shield } from 'lucide-react'

export function RolesPage() {
  const [roles, setRoles] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)

  const load = async () => {
    setLoading(true)
    const r = await api.get('/roles')
    setRoles(r.data)
    setLoading(false)
  }

  useEffect(() => { load() }, [])

  const handleDelete = async (id, nombre) => {
    if (!confirm(`¿Eliminar el rol "${nombre}"?`)) return
    try {
      await api.delete(`/roles/${id}`)
      load()
    } catch (err) {
      alert(err.response?.data?.error || 'No se pudo eliminar el rol')
    }
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Roles</h1>
          <p className="text-muted-foreground text-sm">Roles de usuario y sus permisos</p>
        </div>
        <button onClick={() => { setEditing(null); setShowModal(true) }}
          className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 transition-colors">
          <Plus size={16} /> Nuevo Rol
        </button>
      </div>

      <div className="bg-card border rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b bg-muted/40">
                <th className="text-left p-3 font-medium text-muted-foreground">Nombre</th>
                <th className="text-left p-3 font-medium text-muted-foreground">Descripción</th>
                <th className="p-3 font-medium text-muted-foreground text-right">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={3} className="text-center py-10 text-muted-foreground">
                  <Loader2 className="animate-spin inline mr-2" size={16} />Cargando...
                </td></tr>
              ) : roles.length === 0 ? (
                <tr><td colSpan={3} className="text-center py-12 text-muted-foreground">
                  <Shield size={32} className="mx-auto mb-2 opacity-30" />
                  Sin roles registrados
                </td></tr>
              ) : roles.map(r => (
                <tr key={r.id} className="border-b hover:bg-muted/20">
                  <td className="p-3 font-medium">{r.nombre}</td>
                  <td className="p-3 text-muted-foreground">{r.descripcion || '—'}</td>
                  <td className="p-3">
                    <div className="flex justify-end gap-1">
                      <button onClick={() => { setEditing(r); setShowModal(true) }}
                        className="p-1.5 hover:bg-muted rounded-lg transition-colors text-muted-foreground hover:text-foreground">
                        <Edit2 size={14} />
                      </button>
                      <button onClick={() => handleDelete(r.id, r.nombre)}
                        className="p-1.5 hover:bg-red-50 rounded-lg transition-colors text-muted-foreground hover:text-red-600">
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <RolModal rol={editing} onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load() }} />
      )}
    </div>
  )
}

function RolModal({ rol, onClose, onSaved }) {
  const [error, setError] = useState('')
  const { register, handleSubmit, formState: { isSubmitting } } = useForm({ defaultValues: rol || {} })

  const onSubmit = async (data) => {
    setError('')
    try {
      if (rol) await api.put(`/roles/${rol.id}`, data)
      else await api.post('/roles', data)
      onSaved()
    } catch (err) {
      setError(err.response?.data?.error || 'Error al guardar el rol')
    }
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-sm animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-semibold">{rol ? 'Editar' : 'Nuevo'} Rol</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="p-5 space-y-4">
          {error && <p className="text-sm text-red-600 bg-red-50 border border-red-200 rounded-lg p-2.5">{error}</p>}
          <div>
            <label className="block text-sm font-medium mb-1.5">Nombre *</label>
            <input {...register('nombre', { required: true })}
              className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1.5">Descripción</label>
            <textarea {...register('descripcion')} rows={2}
              className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30 resize-none" />
          </div>
          <div className="flex gap-3">
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

export default RolesPage
