const { query } = require('../db/pool');

const getAll = async (req, res, next) => {
  try {
    const result = await query(
      'SELECT id, nombre, correo, telefono, celular, direccion, fecha_nacimiento FROM empleados WHERE activo = true ORDER BY nombre'
    );
    res.json(result.rows);
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  try {
    const { nombre, direccion, telefono, correo, celular, fecha_nacimiento } = req.body;
    if (!nombre) return res.status(400).json({ error: 'Nombre es requerido' });

    const result = await query(
      'INSERT INTO empleados (nombre, direccion, telefono, correo, celular, fecha_nacimiento) VALUES ($1,$2,$3,$4,$5,$6) RETURNING *',
      [nombre, direccion, telefono, correo, celular, fecha_nacimiento]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const update = async (req, res, next) => {
  try {
    const { nombre, direccion, telefono, correo, celular } = req.body;
    const result = await query(
      `UPDATE empleados
       SET nombre = COALESCE($1, nombre),
           direccion = COALESCE($2, direccion),
           telefono = COALESCE($3, telefono),
           correo = COALESCE($4, correo),
           celular = COALESCE($5, celular)
       WHERE id = $6 AND activo = true RETURNING *`,
      [nombre, direccion, telefono, correo, celular, req.params.id]
    );

    if (!result.rows.length) return res.status(404).json({ error: 'Empleado no encontrado' });
    res.json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const remove = async (req, res, next) => {
  try {
    await query('UPDATE empleados SET activo = false WHERE id = $1', [req.params.id]);
    res.json({ message: 'Empleado desactivado' });
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll, create, update, remove };
