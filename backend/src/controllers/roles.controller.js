const { query } = require('../db/pool');

const getAll = async (req, res, next) => {
  try {
    const result = await query('SELECT id, nombre, descripcion FROM roles ORDER BY id');
    res.json(result.rows);
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  try {
    const { nombre, descripcion, observaciones } = req.body;
    if (!nombre) return res.status(400).json({ error: 'Nombre es requerido' });

    const result = await query(
      'INSERT INTO roles (nombre, descripcion, observaciones) VALUES ($1, $2, $3) RETURNING id, nombre, descripcion',
      [nombre, descripcion, observaciones]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const update = async (req, res, next) => {
  try {
    const { nombre, descripcion, observaciones } = req.body;
    const result = await query(
      `UPDATE roles
       SET nombre = COALESCE($1, nombre),
           descripcion = COALESCE($2, descripcion),
           observaciones = COALESCE($3, observaciones),
           updated_at = NOW()
       WHERE id = $4 RETURNING id, nombre, descripcion`,
      [nombre, descripcion, observaciones, req.params.id]
    );

    if (!result.rows.length) return res.status(404).json({ error: 'Rol no encontrado' });
    res.json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const remove = async (req, res, next) => {
  try {
    const result = await query('DELETE FROM roles WHERE id = $1 RETURNING id', [req.params.id]);
    if (!result.rows.length) return res.status(404).json({ error: 'Rol no encontrado' });
    res.status(204).send();
  } catch (err) {
    if (err.code === '23503') {
      return res.status(409).json({ error: 'No se puede eliminar: el rol está asignado a uno o más usuarios' });
    }
    next(err);
  }
};

module.exports = { getAll, create, update, remove };
