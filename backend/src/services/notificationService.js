const { query } = require('../db/pool');
const { emitirNotificacion } = require('../socket');

/**
 * Inserta notificaciones en bulk para una lista de usuarios y emite via socket.
 * Un solo INSERT reemplaza el N+1 loop anterior.
 */
async function notificarUsuarios(userIds, tipo, mensaje, referenciaId) {
  if (!userIds.length) return;

  // Construir VALUES para inserción masiva
  const values = userIds.map((_, i) => `($${i * 4 + 1}, $${i * 4 + 2}, $${i * 4 + 3}, $${i * 4 + 4})`).join(', ');
  const params = userIds.flatMap(id => [id, tipo, mensaje, referenciaId]);

  await query(
    `INSERT INTO notificaciones (usuario_id, tipo, mensaje, referencia_id) VALUES ${values}`,
    params
  );

  userIds.forEach(id => emitirNotificacion(id, tipo, { referenciaId }));
}

async function notificarStockBajo(insumo) {
  try {
    const result = await query(
      `SELECT u.id FROM usuarios u
       JOIN roles r ON u.rol_id = r.id
       WHERE r.nombre IN ('Administrador', 'Abastecedor') AND u.activo = true`
    );
    const userIds = result.rows.map(r => r.id);
    const mensaje = `Stock bajo: ${insumo.nombre} (${insumo.existencia} ${insumo.unidad_medida} restantes)`;
    await notificarUsuarios(userIds, 'stock_minimo', mensaje, insumo.id);
  } catch {
    // Notificaciones no críticas: no interrumpen el flujo principal
  }
}

async function notificarPedidoNuevo(pedidoId) {
  const result = await query(
    `SELECT u.id FROM usuarios u
     JOIN roles r ON u.rol_id = r.id
     WHERE r.nombre IN ('Administrador', 'Suministrador') AND u.activo = true`
  );
  const userIds = result.rows.map(r => r.id);
  await notificarUsuarios(userIds, 'pedido_nuevo', `Nuevo pedido al sub-almacén #${pedidoId}`, pedidoId);
}

async function notificarPedidoAtendido(solicitanteId, pedidoId) {
  await notificarUsuarios([solicitanteId], 'pedido_atendido', `Tu pedido #${pedidoId} ha sido atendido`, pedidoId);
}

module.exports = { notificarStockBajo, notificarPedidoNuevo, notificarPedidoAtendido };
