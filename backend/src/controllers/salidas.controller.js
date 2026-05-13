const { query } = require('../db/pool');

const getAll = async (req, res, next) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const safeLimit = Math.min(parseInt(limit) || 20, 100);
    const offset = (Math.max(1, parseInt(page)) - 1) * safeLimit;

    const result = await query(
      `SELECT s.id, s.observaciones, s.fecha_registro,
              sv.nombre AS servicio_nombre,
              emp.nombre AS usuario_nombre
       FROM salidas s
       JOIN servicios sv ON s.servicio_id = sv.id
       JOIN usuarios u ON s.usuario_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       ORDER BY s.fecha_registro DESC
       LIMIT $1 OFFSET $2`,
      [safeLimit, offset]
    );

    res.json({ data: result.rows });
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll };
