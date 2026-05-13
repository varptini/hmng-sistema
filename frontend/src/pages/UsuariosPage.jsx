// UsuariosPage, EmpleadosPage, ServiciosPage, BitacoraPage, PedidosAlmacenPage

// ─── USUARIOS PAGE ────────────────────────────────────────────
import { useEffect, useState, useCallback } from 'react'
import { useForm } from 'react-hook-form'
import api from '../lib/api'
import { formatDateTime, cn } from '../lib/utils'
import { Plus, Edit2, UserX, X, Loader2, Users } from 'lucide-react'

export function UsuariosPage() {
  const [usuarios, setUsuarios] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState(null)

  const load = async () => {
    setLoading(true)
    const r = await api.get('/usuarios')
    setUsuarios(r.data)
    setLoading(false)
  }

  useEffect(() => { load() }, [])

  const desactivar = async (id, nombre) => {
    if (!confirm(`¿Desactivar usuario "${nombre}"?`)) return
    await api.delete(`/usuarios/${id}`)
    load()
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="text-2xl font-bold">Usuarios</h1>
          <p className="text-muted-foreground text-sm">Gestión de acceso al sistema</p>
        </div>
        <button onClick={() => { setEditing(null); setShowModal(true) }}
          className="flex items-center gap-2 px-4 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90">
          <Plus size={16} /> Nuevo Usuario
        </button>
      </div>

      <div className="bg-card border rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-muted/40">
              {['Usuario', 'Nombre', 'Rol', 'Estado', 'Último Acceso', ''].map(h => (
                <th key={h} className="text-left p-3 font-medium text-muted-foreground">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan={6} className="text-center py-10 text-muted-foreground">
                <Loader2 className="animate-spin inline mr-2" size={16} />Cargando...
              </td></tr>
            ) : usuarios.map(u => (
              <tr key={u.id} className="border-b hover:bg-muted/20">
                <td className="p-3 font-mono text-sm">{u.nombre_usuario}</td>
                <td className="p-3">{u.nombre}</td>
                <td className="p-3">
                  <span className="px-2 py-0.5 bg-blue-50 text-blue-700 rounded-full text-xs">{u.rol}</span>
                </td>
                <td className="p-3">
                  <span className={cn('px-2 py-0.5 rounded-full text-xs', u.activo ? 'bg-emerald-50 text-emerald-700' : 'bg-gray-100 text-gray-500')}>
                    {u.activo ? 'Activo' : 'Inactivo'}
                  </span>
                </td>
                <td className="p-3 text-muted-foreground">{formatDateTime(u.ultimo_acceso)}</td>
                <td className="p-3">
                  <div className="flex justify-end gap-1">
                    <button onClick={() => { setEditing(u); setShowModal(true) }}
                      className="p-1.5 hover:bg-muted rounded-lg text-muted-foreground hover:text-foreground">
                      <Edit2 size={14} />
                    </button>
                    {u.activo && (
                      <button onClick={() => desactivar(u.id, u.nombre_usuario)}
                        className="p-1.5 hover:bg-red-50 rounded-lg text-muted-foreground hover:text-red-600">
                        <UserX size={14} />
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && <UsuarioModal usuario={editing} onClose={() => setShowModal(false)} onSaved={() => { setShowModal(false); load() }} />}
    </div>
  )
}

function UsuarioModal({ usuario, onClose, onSaved }) {
  const [roles, setRoles] = useState([])
  const [empleados, setEmpleados] = useState([])
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: usuario ? { nombre_usuario: usuario.nombre_usuario, rol_id: usuario.rol_id, activo: usuario.activo } : {}
  })

  useEffect(() => {
    Promise.all([api.get('/roles'), api.get('/empleados')]).then(([r, e]) => { setRoles(r.data); setEmpleados(e.data) })
  }, [])

  const onSubmit = async (data) => {
    if (!data.contrasena) delete data.contrasena
    if (usuario) { await api.put(`/usuarios/${usuario.id}`, data) }
    else { await api.post('/usuarios', data) }
    onSaved()
  }

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-card rounded-xl shadow-xl w-full max-w-md animate-slide-in">
        <div className="flex items-center justify-between p-5 border-b">
          <h2 className="font-semibold">{usuario ? 'Editar Usuario' : 'Nuevo Usuario'}</h2>
          <button onClick={onClose} className="p-1 hover:bg-muted rounded-lg"><X size={16} /></button>
        </div>
        <form onSubmit={handleSubmit(onSubmit)} className="p-5 space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1.5">Nombre de usuario *</label>
            <input {...register('nombre_usuario', { required: 'Requerido' })}
              className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1.5">{usuario ? 'Nueva contraseña (dejar vacío para no cambiar)' : 'Contraseña *'}</label>
            <input {...register('contrasena', { required: !usuario ? 'Requerido' : false, minLength: { value: 8, message: 'Mínimo 8 caracteres' } })}
              type="password"
              className="w-full px-3 py-2 border rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-primary/30" />
            {errors.contrasena && <p className="text-xs text-red-500 mt-1">{errors.contrasena.message}</p>}
          </div>
          {!usuario && (
            <div>
              <label className="block text-sm font-medium mb-1.5">Empleado *</label>
              <select {...register('empleado_id', { required: !usuario })}
                className="w-full px-3 py-2 border rounded-lg text-sm bg-background focus:outline-none focus:ring-2 focus:ring-primary/30">
                <option value="">Seleccionar empleado</option>
                {empleados.map(e => <option key={e.id} value={e.id}>{e.nombre}</option>)}
              </select>
            </div>
          )}
          <div>
            <label className="block text-sm font-medium mb-1.5">Rol *</label>
            <select {...register('rol_id', { required: 'Requerido' })}
              className="w-full px-3 py-2 border rounded-lg text-sm bg-background focus:outline-none focus:ring-2 focus:ring-primary/30">
              <option value="">Seleccionar rol</option>
              {roles.map(r => <option key={r.id} value={r.id}>{r.nombre}</option>)}
            </select>
          </div>
          {usuario && (
            <div className="flex items-center gap-2">
              <input type="checkbox" {...register('activo')} id="activo" className="w-4 h-4" />
              <label htmlFor="activo" className="text-sm">Usuario activo</label>
            </div>
          )}
          <div className="flex gap-3 pt-2">
            <button type="button" onClick={onClose} className="flex-1 py-2 border rounded-lg text-sm hover:bg-muted">Cancelar</button>
            <button type="submit" disabled={isSubmitting}
              className="flex-1 py-2 bg-primary text-primary-foreground rounded-lg text-sm font-medium hover:bg-primary/90 disabled:opacity-50 flex items-center justify-center gap-2">
              {isSubmitting && <Loader2 size={14} className="animate-spin" />}
              {usuario ? 'Guardar' : 'Crear usuario'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default UsuariosPage
