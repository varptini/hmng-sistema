const bcrypt = require('bcryptjs');
const { query } = require('../db/pool');
const { registrarAuditoria } = require('../utils/auditoria');

const getAll = async (req, res, next) => {
  try {
    const result = await query(
      `SELECT u.id, u.nombre_usuario, u.activo, u.ultimo_acceso, u.created_at,
              e.nombre, e.correo, e.telefono, e.celular,
              r.nombre as rol, r.id as rol_id
       FROM usuarios u
       JOIN empleados e ON u.empleado_id = e.id
       JOIN roles r ON u.rol_id = r.id
       ORDER BY e.nombre`
    );
    res.json(result.rows);
  } catch (err) { next(err); }
};

const getById = async (req, res, next) => {
  try {
    const result = await query(
      `SELECT u.id, u.nombre_usuario, u.activo, u.empleado_id, u.rol_id, u.ultimo_acceso,
              e.nombre, e.correo, e.telefono, e.celular, e.direccion, e.fecha_nacimiento,
              r.nombre as rol
       FROM usuarios u
       JOIN empleados e ON u.empleado_id = e.id
       JOIN roles r ON u.rol_id = r.id
       WHERE u.id = $1`,
      [req.params.id]
    );
    if (!result.rows.length) return res.status(404).json({ error: 'Usuario no encontrado' });
    res.json(result.rows[0]);
  } catch (err) { next(err); }
};

const create = async (req, res, next) => {
  try {
    const { nombre_usuario, contrasena, empleado_id, rol_id } = req.body;

    if (!nombre_usuario || !contrasena || !empleado_id || !rol_id) {
      return res.status(400).json({ error: 'Todos los campos son requeridos' });
    }
    if (contrasena.length < 8) {
      return res.status(400).json({ error: 'La contraseña debe tener al menos 8 caracteres' });
    }

    const hash = await bcrypt.hash(contrasena, 12);
    const result = await query(
      `INSERT INTO usuarios (nombre_usuario, contrasena, empleado_id, rol_id)
       VALUES ($1, $2, $3, $4) RETURNING id, nombre_usuario, activo, created_at`,
      [nombre_usuario, hash, empleado_id, rol_id]
    );

    await registrarAuditoria(req.user.id, 'CREAR_USUARIO', 'usuarios', `Usuario creado: ${nombre_usuario}`);
    res.status(201).json(result.rows[0]);
  } catch (err) { next(err); }
};

const update = async (req, res, next) => {
  try {
    const { nombre_usuario, rol_id, activo, contrasena } = req.body;
    const { id } = req.params;

    let updateFields = [];
    const params = [];

    if (nombre_usuario) { params.push(nombre_usuario); updateFields.push(`nombre_usuario = $${params.length}`); }
    if (rol_id) { params.push(rol_id); updateFields.push(`rol_id = $${params.length}`); }
    if (typeof activo === 'boolean') { params.push(activo); updateFields.push(`activo = $${params.length}`); }
    if (contrasena) {
      if (contrasena.length < 8) return res.status(400).json({ error: 'Contraseña mínimo 8 caracteres' });
      const hash = await bcrypt.hash(contrasena, 12);
      params.push(hash);
      updateFields.push(`contrasena = $${params.length}`);
    }

    if (!updateFields.length) return res.status(400).json({ error: 'Sin campos para actualizar' });

    params.push(id);
    const result = await query(
      `UPDATE usuarios SET ${updateFields.join(', ')} WHERE id = $${params.length} RETURNING id, nombre_usuario, activo`,
      params
    );

    if (!result.rows.length) return res.status(404).json({ error: 'Usuario no encontrado' });

    await registrarAuditoria(req.user.id, 'ACTUALIZAR_USUARIO', 'usuarios', `Usuario actualizado: ${result.rows[0].nombre_usuario}`);
    res.json(result.rows[0]);
  } catch (err) { next(err); }
};

const remove = async (req, res, next) => {
  try {
    if (parseInt(req.params.id) === req.user.id) {
      return res.status(400).json({ error: 'No puedes desactivar tu propio usuario' });
    }
    const result = await query(
      `UPDATE usuarios SET activo = false WHERE id = $1 RETURNING nombre_usuario`,
      [req.params.id]
    );
    if (!result.rows.length) return res.status(404).json({ error: 'Usuario no encontrado' });
    await registrarAuditoria(req.user.id, 'DESACTIVAR_USUARIO', 'usuarios', `Usuario desactivado: ${result.rows[0].nombre_usuario}`);
    res.json({ message: 'Usuario desactivado correctamente' });
  } catch (err) { next(err); }
};

module.exports = { getAll, getById, create, update, remove };
