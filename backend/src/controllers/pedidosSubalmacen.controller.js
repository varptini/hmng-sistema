const { query, getClient } = require('../db/pool');
const { registrarAuditoria } = require('../utils/auditoria');
const { notificarPedidoNuevo, notificarPedidoAtendido, notificarStockBajo } = require('../services/notificationService');

const getAll = async (req, res, next) => {
  try {
    const { estado, servicio_id, page = 1, limit = 20 } = req.query;
    const safeLimit = Math.min(parseInt(limit) || 20, 100);
    const offset = (Math.max(1, parseInt(page)) - 1) * safeLimit;
    const params = [];
    const conditions = [];

    if (req.user.rol === 'Responsable de Servicio') {
      params.push(req.user.id);
      conditions.push(`p.usuario_solicitante_id = $${params.length}`);
    }

    if (estado) {
      params.push(estado);
      conditions.push(`p.estado = $${params.length}`);
    }
    if (servicio_id) {
      params.push(servicio_id);
      conditions.push(`p.servicio_id = $${params.length}`);
    }

    const where = conditions.length ? 'WHERE ' + conditions.join(' AND ') : '';
    params.push(safeLimit, offset);

    const result = await query(
      `SELECT p.id, p.estado, p.observaciones, p.fecha_pedido, p.fecha_atencion,
              s.nombre AS servicio_nombre,
              emp_sol.nombre AS solicitante_nombre,
              emp_ate.nombre AS atiende_nombre,
              (SELECT COUNT(*) FROM detalle_pedidos_subalmacen d WHERE d.pedido_subalmacen_id = p.id) AS total_items
       FROM pedidos_subalmacen p
       JOIN servicios s ON p.servicio_id = s.id
       JOIN usuarios u_sol ON p.usuario_solicitante_id = u_sol.id
       JOIN empleados emp_sol ON u_sol.empleado_id = emp_sol.id
       LEFT JOIN usuarios u_ate ON p.usuario_atiende_id = u_ate.id
       LEFT JOIN empleados emp_ate ON u_ate.empleado_id = emp_ate.id
       ${where}
       ORDER BY p.fecha_pedido DESC
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
    const pedido = await query(
      `SELECT p.id, p.estado, p.observaciones, p.fecha_pedido,
              s.nombre AS servicio_nombre,
              emp.nombre AS solicitante_nombre
       FROM pedidos_subalmacen p
       JOIN servicios s ON p.servicio_id = s.id
       JOIN usuarios u ON p.usuario_solicitante_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       WHERE p.id = $1`,
      [req.params.id]
    );

    if (!pedido.rows.length) {
      return res.status(404).json({ error: 'Pedido no encontrado' });
    }

    const detalles = await query(
      `SELECT d.id, d.cantidad_solicitada, d.cantidad_surtida,
              i.nombre AS insumo_nombre, i.unidad_medida, i.existencia
       FROM detalle_pedidos_subalmacen d
       JOIN insumos i ON d.insumo_id = i.id
       WHERE d.pedido_subalmacen_id = $1`,
      [req.params.id]
    );

    res.json({ ...pedido.rows[0], detalles: detalles.rows });
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  const { servicio_id, observaciones, detalles } = req.body;

  // Validaciones antes de abrir transacción
  if (!servicio_id || !detalles?.length) {
    return res.status(400).json({ error: 'Servicio y al menos un insumo son requeridos' });
  }

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const pedido = await client.query(
      `INSERT INTO pedidos_subalmacen (usuario_solicitante_id, servicio_id, observaciones)
       VALUES ($1, $2, $3) RETURNING id`,
      [req.user.id, servicio_id, observaciones]
    );
    const pedidoId = pedido.rows[0].id;

    for (const item of detalles) {
      await client.query(
        `INSERT INTO detalle_pedidos_subalmacen (pedido_subalmacen_id, insumo_id, cantidad_solicitada)
         VALUES ($1, $2, $3)`,
        [pedidoId, item.insumo_id, item.cantidad]
      );
    }

    await client.query('COMMIT');

    // Notificaciones fuera de la transacción: son mejores esfuerzo
    await notificarPedidoNuevo(pedidoId);
    await registrarAuditoria(req.user.id, 'CREAR_PEDIDO', 'pedidos_subalmacen', `Pedido #${pedidoId} creado`);

    res.status(201).json({ id: pedidoId, message: 'Pedido registrado correctamente' });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
};

const atender = async (req, res, next) => {
  const { id } = req.params;
  const { cantidades_surtidas } = req.body;

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const pedido = await client.query(
      'SELECT * FROM pedidos_subalmacen WHERE id = $1 AND estado = $2',
      [id, 'pendiente']
    );

    if (!pedido.rows.length) {
      return res.status(404).json({ error: 'Pedido no encontrado o ya fue atendido' });
    }

    const detalles = await client.query(
      `SELECT d.*, i.existencia, i.cantidad_minima, i.nombre AS insumo_nombre, i.unidad_medida
       FROM detalle_pedidos_subalmacen d
       JOIN insumos i ON d.insumo_id = i.id
       WHERE d.pedido_subalmacen_id = $1`,
      [id]
    );

    const salida = await client.query(
      `INSERT INTO salidas (usuario_id, servicio_id, observaciones)
       VALUES ($1, $2, $3) RETURNING id`,
      [req.user.id, pedido.rows[0].servicio_id, `Atendiendo pedido #${id}`]
    );
    const salidaId = salida.rows[0].id;

    const insumosConStockBajo = [];

    for (const detalle of detalles.rows) {
      const cantidadSurtida = cantidades_surtidas?.[detalle.insumo_id] ?? detalle.cantidad_solicitada;
      if (cantidadSurtida <= 0) continue;

      const existenciaActual = parseFloat(detalle.existencia);
      if (existenciaActual < cantidadSurtida) {
        throw new Error(`Stock insuficiente para: ${detalle.insumo_nombre}`);
      }

      const existenciaNueva = existenciaActual - cantidadSurtida;

      await client.query(
        'UPDATE detalle_pedidos_subalmacen SET cantidad_surtida = $1 WHERE id = $2',
        [cantidadSurtida, detalle.id]
      );
      await client.query(
        'INSERT INTO detalle_salidas (salida_id, insumo_id, cantidad) VALUES ($1, $2, $3)',
        [salidaId, detalle.insumo_id, cantidadSurtida]
      );
      await client.query(
        'UPDATE insumos SET existencia = existencia - $1 WHERE id = $2',
        [cantidadSurtida, detalle.insumo_id]
      );
      await client.query(
        `INSERT INTO bitacora (tipo, insumo_id, cantidad, existencia_anterior, existencia_nueva, referencia_id, referencia_tipo, usuario_id)
         VALUES ('salida', $1, $2, $3, $4, $5, 'pedido_subalmacen', $6)`,
        [detalle.insumo_id, cantidadSurtida, existenciaActual, existenciaNueva, id, req.user.id]
      );

      if (existenciaNueva <= parseFloat(detalle.cantidad_minima)) {
        insumosConStockBajo.push({ id: detalle.insumo_id, nombre: detalle.insumo_nombre, existencia: existenciaNueva, unidad_medida: detalle.unidad_medida });
      }
    }

    await client.query(
      `UPDATE pedidos_subalmacen SET estado = 'atendido', usuario_atiende_id = $1, fecha_atencion = NOW()
       WHERE id = $2`,
      [req.user.id, id]
    );

    await client.query('COMMIT');

    // Notificaciones fuera de la transacción
    await notificarPedidoAtendido(pedido.rows[0].usuario_solicitante_id, id);
    for (const insumo of insumosConStockBajo) {
      await notificarStockBajo(insumo);
    }

    await registrarAuditoria(req.user.id, 'ATENDER_PEDIDO', 'pedidos_subalmacen', `Pedido #${id} atendido`);
    res.json({ message: 'Pedido atendido correctamente' });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
};

const cancelar = async (req, res, next) => {
  try {
    const result = await query(
      `UPDATE pedidos_subalmacen SET estado = 'cancelado'
       WHERE id = $1 AND estado = 'pendiente' RETURNING id`,
      [req.params.id]
    );

    if (!result.rows.length) {
      return res.status(404).json({ error: 'Pedido no encontrado o no se puede cancelar' });
    }

    res.json({ message: 'Pedido cancelado' });
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll, getById, create, atender, cancelar };
