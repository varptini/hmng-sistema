const { query, getClient } = require('../db/pool');
const { registrarAuditoria } = require('../utils/auditoria');
const { notificarStockBajo } = require('../services/notificationService');

const getAll = async (req, res, next) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const safeLimit = Math.min(parseInt(limit) || 20, 100);
    const offset = (Math.max(1, parseInt(page)) - 1) * safeLimit;

    const result = await query(
      `SELECT s.id, s.observaciones, s.fecha_registro,
              sv.nombre AS servicio_nombre,
              emp.nombre AS usuario_nombre,
              (SELECT COUNT(*) FROM detalle_salidas ds WHERE ds.salida_id = s.id) AS total_items
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

const getById = async (req, res, next) => {
  try {
    const salida = await query(
      `SELECT s.id, s.observaciones, s.fecha_registro,
              sv.nombre AS servicio_nombre,
              emp.nombre AS usuario_nombre
       FROM salidas s
       JOIN servicios sv ON s.servicio_id = sv.id
       JOIN usuarios u ON s.usuario_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       WHERE s.id = $1`,
      [req.params.id]
    );

    if (!salida.rows.length) {
      return res.status(404).json({ error: 'Salida no encontrada' });
    }

    const detalles = await query(
      `SELECT ds.id, ds.cantidad, i.nombre AS insumo_nombre, i.unidad_medida
       FROM detalle_salidas ds
       JOIN insumos i ON ds.insumo_id = i.id
       WHERE ds.salida_id = $1`,
      [req.params.id]
    );

    res.json({ ...salida.rows[0], detalles: detalles.rows });
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  const { servicio_id, observaciones, detalles } = req.body;

  if (!servicio_id) {
    return res.status(400).json({ error: 'Se requiere el servicio destino' });
  }
  if (!detalles || !detalles.length) {
    return res.status(400).json({ error: 'Se requiere al menos un insumo' });
  }

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const salida = await client.query(
      `INSERT INTO salidas (usuario_id, servicio_id, observaciones)
       VALUES ($1, $2, $3) RETURNING id`,
      [req.user.id, servicio_id, observaciones]
    );
    const salidaId = salida.rows[0].id;
    const insumosConStockBajo = [];

    for (const item of detalles) {
      const { insumo_id, cantidad } = item;

      if (!insumo_id || !cantidad || cantidad <= 0) {
        throw new Error('Cada detalle requiere insumo_id y cantidad positiva');
      }

      const insumoActual = await client.query(
        'SELECT existencia, cantidad_minima, nombre, unidad_medida FROM insumos WHERE id = $1',
        [insumo_id]
      );

      if (!insumoActual.rows.length) {
        throw new Error(`Insumo ${insumo_id} no encontrado`);
      }

      const { existencia, cantidad_minima, nombre, unidad_medida } = insumoActual.rows[0];
      const existenciaAnterior = parseFloat(existencia);
      const cantidadSolicitada = parseFloat(cantidad);

      if (existenciaAnterior < cantidadSolicitada) {
        throw new Error(`Stock insuficiente para: ${nombre} (disponible: ${existenciaAnterior})`);
      }

      const existenciaNueva = existenciaAnterior - cantidadSolicitada;

      await client.query(
        'INSERT INTO detalle_salidas (salida_id, insumo_id, cantidad) VALUES ($1, $2, $3)',
        [salidaId, insumo_id, cantidad]
      );

      await client.query(
        'UPDATE insumos SET existencia = existencia - $1 WHERE id = $2',
        [cantidad, insumo_id]
      );

      await client.query(
        `INSERT INTO bitacora (tipo, insumo_id, cantidad, existencia_anterior, existencia_nueva, referencia_id, referencia_tipo, usuario_id)
         VALUES ('salida', $1, $2, $3, $4, $5, 'salida', $6)`,
        [insumo_id, cantidad, existenciaAnterior, existenciaNueva, salidaId, req.user.id]
      );

      if (existenciaNueva <= parseFloat(cantidad_minima)) {
        insumosConStockBajo.push({ id: insumo_id, nombre, existencia: existenciaNueva, unidad_medida });
      }
    }

    await client.query('COMMIT');

    for (const insumo of insumosConStockBajo) {
      await notificarStockBajo(insumo);
    }
    await registrarAuditoria(req.user.id, 'CREAR_SALIDA', 'salidas', `Salida #${salidaId} registrada`);

    res.status(201).json({ id: salidaId, message: 'Salida registrada correctamente' });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
};

module.exports = { getAll, getById, create };
