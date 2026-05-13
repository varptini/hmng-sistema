const { query } = require('../db/pool');

const getAll = async (req, res, next) => {
  try {
    const result = await query('SELECT id, nombre, descripcion, activo FROM servicios WHERE activo = true ORDER BY nombre');
    res.json(result.rows);
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  try {
    const { nombre, descripcion } = req.body;
    if (!nombre) return res.status(400).json({ error: 'Nombre es requerido' });

    const result = await query(
      'INSERT INTO servicios (nombre, descripcion) VALUES ($1, $2) RETURNING *',
      [nombre, descripcion]
    );
    res.status(201).json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const update = async (req, res, next) => {
  try {
    const { nombre, descripcion, activo } = req.body;
    const result = await query(
      `UPDATE servicios
       SET nombre = COALESCE($1, nombre),
           descripcion = COALESCE($2, descripcion),
           activo = COALESCE($3, activo)
       WHERE id = $4 RETURNING *`,
      [nombre, descripcion, activo, req.params.id]
    );

    if (!result.rows.length) return res.status(404).json({ error: 'Servicio no encontrado' });
    res.json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll, create, update };
