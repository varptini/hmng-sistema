const { query } = require('../db/pool');

const getInventario = async (req, res, next) => {
  try {
    const result = await query(
      `SELECT id, nombre, descripcion, unidad_medida, existencia, cantidad_minima, lote, fecha_caducidad,
              CASE WHEN fecha_caducidad < NOW() THEN 'caducado'
                   WHEN fecha_caducidad <= NOW() + INTERVAL '30 days' THEN 'por_caducar'
                   ELSE 'vigente' END AS estado_caducidad,
              CASE WHEN existencia = 0 THEN 'agotado'
                   WHEN existencia <= cantidad_minima THEN 'stock_bajo'
                   ELSE 'normal' END AS estado_stock
       FROM insumos WHERE activo = true ORDER BY nombre`
    );
    res.json({ data: result.rows, fecha_reporte: new Date() });
  } catch (err) {
    next(err);
  }
};

const getMovimientos = async (req, res, next) => {
  try {
    const { desde, hasta } = req.query;
    const params = desde && hasta ? [desde, hasta] : [];
    const where = params.length ? 'WHERE b.fecha BETWEEN $1 AND $2' : '';

    const result = await query(
      `SELECT b.id, b.tipo, b.cantidad, b.existencia_anterior, b.existencia_nueva, b.referencia_tipo, b.fecha,
              i.nombre AS insumo_nombre, i.unidad_medida,
              emp.nombre AS usuario_nombre
       FROM bitacora b
       JOIN insumos i ON b.insumo_id = i.id
       JOIN usuarios u ON b.usuario_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       ${where}
       ORDER BY b.fecha DESC`,
      params
    );

    res.json({ data: result.rows, fecha_reporte: new Date() });
  } catch (err) {
    next(err);
  }
};

module.exports = { getInventario, getMovimientos };
