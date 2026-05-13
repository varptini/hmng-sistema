const { query } = require('../db/pool');

function escapeLike(text) {
  return text.replace(/[%_\\]/g, '\\$&');
}

const getAll = async (req, res, next) => {
  try {
    const { insumo_id, tipo, desde, hasta, page = 1, limit = 30 } = req.query;
    const safeLimit = Math.min(parseInt(limit) || 30, 100);
    const offset = (Math.max(1, parseInt(page)) - 1) * safeLimit;

    const params = [];
    const conditions = [];

    if (insumo_id) { params.push(insumo_id); conditions.push(`b.insumo_id = $${params.length}`); }
    if (tipo) { params.push(tipo); conditions.push(`b.tipo = $${params.length}`); }
    if (desde) { params.push(desde); conditions.push(`b.fecha >= $${params.length}`); }
    if (hasta) { params.push(hasta); conditions.push(`b.fecha <= $${params.length}`); }

    const where = conditions.length ? 'WHERE ' + conditions.join(' AND ') : '';
    params.push(safeLimit, offset);

    const result = await query(
      `SELECT b.id, b.tipo, b.cantidad, b.existencia_anterior, b.existencia_nueva, b.referencia_tipo, b.fecha,
              i.nombre AS insumo_nombre, i.unidad_medida,
              emp.nombre AS usuario_nombre
       FROM bitacora b
       JOIN insumos i ON b.insumo_id = i.id
       JOIN usuarios u ON b.usuario_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       ${where}
       ORDER BY b.fecha DESC
       LIMIT $${params.length - 1} OFFSET $${params.length}`,
      params
    );

    res.json({ data: result.rows });
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll };
