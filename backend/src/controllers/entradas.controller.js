const { query, getClient } = require('../db/pool');
const { registrarAuditoria } = require('../utils/auditoria');

const getAll = async (req, res, next) => {
  try {
    const { page = 1, limit = 20, desde, hasta } = req.query;
    const safeLimit = Math.min(parseInt(limit) || 20, 100);
    const offset = (parseInt(page) - 1) * safeLimit;
    const params = [];
    let where = '';

    if (desde && hasta) {
      params.push(desde, hasta);
      where = `WHERE e.fecha_registro BETWEEN $1 AND $2`;
    }

    params.push(safeLimit, offset);
    const result = await query(
      `SELECT e.id, e.observaciones, e.fecha_registro,
              u.nombre_usuario, emp.nombre AS usuario_nombre,
              ag.nombre AS almacen_nombre,
              (SELECT COUNT(*) FROM detalle_entradas de WHERE de.entrada_id = e.id) AS total_items
       FROM entradas e
       JOIN usuarios u ON e.usuario_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       LEFT JOIN almacen_general ag ON e.almacen_general_id = ag.id
       ${where}
       ORDER BY e.fecha_registro DESC
       LIMIT $${params.length - 1} OFFSET $${params.length}`,
      params
    );

    res.json({ data: result.rows });
  } catch (err) {
    next(err);
  }
};

const getById = async (req, res, next) => {
  try {
    const entrada = await query(
      `SELECT e.id, e.observaciones, e.fecha_registro,
              u.nombre_usuario, emp.nombre AS usuario_nombre,
              ag.nombre AS almacen_nombre
       FROM entradas e
       JOIN usuarios u ON e.usuario_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       LEFT JOIN almacen_general ag ON e.almacen_general_id = ag.id
       WHERE e.id = $1`,
      [req.params.id]
    );

    if (!entrada.rows.length) {
      return res.status(404).json({ error: 'Entrada no encontrada' });
    }

    const detalles = await query(
      `SELECT de.id, de.cantidad, de.lote, de.fecha_caducidad, de.precio_unitario,
              i.nombre AS insumo_nombre, i.unidad_medida
       FROM detalle_entradas de
       JOIN insumos i ON de.insumo_id = i.id
       WHERE de.entrada_id = $1`,
      [req.params.id]
    );

    res.json({ ...entrada.rows[0], detalles: detalles.rows });
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  const { almacen_general_id, observaciones, detalles } = req.body;

  // Validaciones ANTES de abrir la transacción
  if (!detalles || !detalles.length) {
    return res.status(400).json({ error: 'Se requiere al menos un insumo' });
  }

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const entrada = await client.query(
      `INSERT INTO entradas (usuario_id, almacen_general_id, observaciones)
       VALUES ($1, $2, $3) RETURNING id`,
      [req.user.id, almacen_general_id, observaciones]
    );
    const entradaId = entrada.rows[0].id;

    for (const item of detalles) {
      const { insumo_id, cantidad, lote, fecha_caducidad, precio_unitario } = item;

      if (!insumo_id || !cantidad || cantidad <= 0) {
        throw new Error('Cada detalle requiere insumo_id y cantidad positiva');
      }

      await client.query(
        `INSERT INTO detalle_entradas (entrada_id, insumo_id, cantidad, lote, fecha_caducidad, precio_unitario)
         VALUES ($1, $2, $3, $4, $5, $6)`,
        [entradaId, insumo_id, cantidad, lote, fecha_caducidad, precio_unitario]
      );

      const insumoActual = await client.query(
        'SELECT existencia, nombre FROM insumos WHERE id = $1',
        [insumo_id]
      );

      if (!insumoActual.rows.length) {
        throw new Error(`Insumo ${insumo_id} no encontrado`);
      }

      const existenciaAnterior = parseFloat(insumoActual.rows[0].existencia);
      const existenciaNueva = existenciaAnterior + parseFloat(cantidad);

      await client.query(
        `UPDATE insumos
         SET existencia = existencia + $1,
             lote = COALESCE($2, lote),
             fecha_caducidad = COALESCE($3, fecha_caducidad)
         WHERE id = $4`,
        [cantidad, lote, fecha_caducidad, insumo_id]
      );

      await client.query(
        `INSERT INTO bitacora (tipo, insumo_id, cantidad, existencia_anterior, existencia_nueva, referencia_id, referencia_tipo, usuario_id)
         VALUES ('entrada', $1, $2, $3, $4, $5, 'entrada', $6)`,
        [insumo_id, cantidad, existenciaAnterior, existenciaNueva, entradaId, req.user.id]
      );
    }

    await client.query('COMMIT');
    await registrarAuditoria(req.user.id, 'CREAR_ENTRADA', 'entradas', `Entrada #${entradaId} registrada`);

    res.status(201).json({ id: entradaId, message: 'Entrada registrada correctamente' });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
};

module.exports = { getAll, getById, create };
