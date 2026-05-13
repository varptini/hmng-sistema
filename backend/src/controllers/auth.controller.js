const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { query } = require('../db/pool');
const { registrarAuditoria } = require('../utils/auditoria');

const login = async (req, res, next) => {
  try {
    const { nombre_usuario, contrasena } = req.body;

    if (!nombre_usuario || !contrasena) {
      return res.status(400).json({ error: 'Usuario y contraseña son requeridos' });
    }

    // Buscar usuario con datos del empleado y rol
    const result = await query(
      `SELECT u.id, u.nombre_usuario, u.contrasena, u.activo,
              e.nombre as empleado_nombre, e.correo,
              r.nombre as rol, r.id as rol_id
       FROM usuarios u
       JOIN empleados e ON u.empleado_id = e.id
       JOIN roles r ON u.rol_id = r.id
       WHERE u.nombre_usuario = $1`,
      [nombre_usuario]
    );

    if (!result.rows.length) {
      return res.status(401).json({ error: 'Credenciales incorrectas' });
    }

    const usuario = result.rows[0];

    if (!usuario.activo) {
      return res.status(401).json({ error: 'Usuario inactivo. Contacta al administrador.' });
    }

    const passwordOk = await bcrypt.compare(contrasena, usuario.contrasena);
    if (!passwordOk) {
      return res.status(401).json({ error: 'Credenciales incorrectas' });
    }

    // Actualizar último acceso
    await query('UPDATE usuarios SET ultimo_acceso = NOW() WHERE id = $1', [usuario.id]);

    const token = jwt.sign(
      { id: usuario.id, rol: usuario.rol },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN || '8h' }
    );

    await registrarAuditoria(usuario.id, 'LOGIN', 'usuarios', `Inicio de sesión: ${nombre_usuario}`);

    res.json({
      token,
      usuario: {
        id: usuario.id,
        nombre_usuario: usuario.nombre_usuario,
        nombre: usuario.empleado_nombre,
        correo: usuario.correo,
        rol: usuario.rol,
        rol_id: usuario.rol_id
      }
    });
  } catch (err) {
    next(err);
  }
};

const getProfile = async (req, res, next) => {
  try {
    const result = await query(
      `SELECT u.id, u.nombre_usuario, u.ultimo_acceso,
              e.nombre, e.correo, e.telefono, e.celular,
              r.nombre as rol, r.descripcion as rol_descripcion
       FROM usuarios u
       JOIN empleados e ON u.empleado_id = e.id
       JOIN roles r ON u.rol_id = r.id
       WHERE u.id = $1`,
      [req.user.id]
    );

    if (!result.rows.length) {
      return res.status(404).json({ error: 'Usuario no encontrado' });
    }

    res.json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const cambiarContrasena = async (req, res, next) => {
  try {
    const { contrasena_actual, contrasena_nueva } = req.body;

    if (!contrasena_actual || !contrasena_nueva) {
      return res.status(400).json({ error: 'Ambas contraseñas son requeridas' });
    }

    if (contrasena_nueva.length < 8) {
      return res.status(400).json({ error: 'La nueva contraseña debe tener al menos 8 caracteres' });
    }

    const result = await query('SELECT contrasena FROM usuarios WHERE id = $1', [req.user.id]);
    const ok = await bcrypt.compare(contrasena_actual, result.rows[0].contrasena);

    if (!ok) {
      return res.status(400).json({ error: 'Contraseña actual incorrecta' });
    }

    const hash = await bcrypt.hash(contrasena_nueva, 12);
    await query('UPDATE usuarios SET contrasena = $1 WHERE id = $2', [hash, req.user.id]);

    await registrarAuditoria(req.user.id, 'CAMBIO_CONTRASENA', 'usuarios', 'Cambio de contraseña propio');

    res.json({ message: 'Contraseña actualizada correctamente' });
  } catch (err) {
    next(err);
  }
};

module.exports = { login, getProfile, cambiarContrasena };
