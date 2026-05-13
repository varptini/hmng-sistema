const { query, getClient } = require('../db/pool');
const { registrarAuditoria } = require('../utils/auditoria');

const getAll = async (req, res, next) => {
  try {
    const result = await query(
      `SELECT p.id, p.estado, p.observaciones, p.fecha_pedido, p.fecha_recepcion,
              ag.nombre AS almacen_nombre,
              emp.nombre AS usuario_nombre
       FROM pedidos_almacen_general p
       LEFT JOIN almacen_general ag ON p.almacen_general_id = ag.id
       JOIN usuarios u ON p.usuario_id = u.id
       JOIN empleados emp ON u.empleado_id = emp.id
       ORDER BY p.fecha_pedido DESC`
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
              ag.nombre AS almacen_nombre
       FROM pedidos_almacen_general p
       LEFT JOIN almacen_general ag ON p.almacen_general_id = ag.id
       WHERE p.id = $1`,
      [req.params.id]
    );

    if (!pedido.rows.length) return res.status(404).json({ error: 'Pedido no encontrado' });

    const detalles = await query(
      `SELECT d.id, d.cantidad_solicitada, i.nombre AS insumo_nombre, i.unidad_medida
       FROM detalle_pedidos_almacen d
       JOIN insumos i ON d.insumo_id = i.id
       WHERE d.pedido_almacen_id = $1`,
      [req.params.id]
    );

    res.json({ ...pedido.rows[0], detalles: detalles.rows });
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  const { almacen_general_id, observaciones, detalles } = req.body;

  if (!detalles?.length) {
    return res.status(400).json({ error: 'Se requiere al menos un insumo' });
  }

  const client = await getClient();
  try {
    await client.query('BEGIN');

    const pedido = await client.query(
      'INSERT INTO pedidos_almacen_general (usuario_id, almacen_general_id, observaciones) VALUES ($1,$2,$3) RETURNING id',
      [req.user.id, almacen_general_id, observaciones]
    );
    const pedidoId = pedido.rows[0].id;

    for (const item of detalles) {
      await client.query(
        'INSERT INTO detalle_pedidos_almacen (pedido_almacen_id, insumo_id, cantidad_solicitada) VALUES ($1,$2,$3)',
        [pedidoId, item.insumo_id, item.cantidad]
      );
    }

    await client.query('COMMIT');
    await registrarAuditoria(req.user.id, 'CREAR_PEDIDO_ALMACEN', 'pedidos_almacen_general', `Pedido #${pedidoId}`);
    res.status(201).json({ id: pedidoId, message: 'Pedido creado' });
  } catch (err) {
    await client.query('ROLLBACK');
    next(err);
  } finally {
    client.release();
  }
};

const updateEstado = async (req, res, next) => {
  try {
    const { estado } = req.body;
    const estadosValidos = ['pendiente', 'enviado', 'recibido', 'cancelado'];

    if (!estadosValidos.includes(estado)) {
      return res.status(400).json({ error: `Estado inválido. Valores permitidos: ${estadosValidos.join(', ')}` });
    }

    const extra = estado === 'recibido' ? ', fecha_recepcion = NOW()' : '';
    const result = await query(
      `UPDATE pedidos_almacen_general SET estado = $1${extra} WHERE id = $2 RETURNING id`,
      [estado, req.params.id]
    );

    if (!result.rows.length) return res.status(404).json({ error: 'Pedido no encontrado' });
    res.json({ message: 'Estado actualizado' });
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll, getById, create, updateEstado };
