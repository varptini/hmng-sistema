const { query } = require('../db/pool');

const getStats = async (req, res, next) => {
  try {
    const [
      totalInsumos,
      porCaducar,
      caducados,
      stockBajo,
      pedidosPendientes,
      pedidosAlmacenPendientes,
      entradasMes,
      salidasMes,
      movimientosRecientes
    ] = await Promise.all([
      query('SELECT COUNT(*) FROM insumos WHERE activo = true'),
      query(`SELECT COUNT(*) FROM insumos WHERE activo = true AND fecha_caducidad BETWEEN NOW() AND NOW() + INTERVAL '30 days'`),
      query(`SELECT COUNT(*) FROM insumos WHERE activo = true AND fecha_caducidad < NOW()`),
      query('SELECT COUNT(*) FROM insumos WHERE activo = true AND existencia <= cantidad_minima AND existencia > 0'),
      query(`SELECT COUNT(*) FROM pedidos_subalmacen WHERE estado = 'pendiente'`),
      query(`SELECT COUNT(*) FROM pedidos_almacen_general WHERE estado IN ('pendiente','enviado')`),
      query(`SELECT COALESCE(SUM(de.cantidad), 0) as total
             FROM entradas e JOIN detalle_entradas de ON e.id = de.entrada_id
             WHERE DATE_TRUNC('month', e.fecha_registro) = DATE_TRUNC('month', NOW())`),
      query(`SELECT COALESCE(SUM(ds.cantidad), 0) as total
             FROM salidas s JOIN detalle_salidas ds ON s.id = ds.salida_id
             WHERE DATE_TRUNC('month', s.fecha_registro) = DATE_TRUNC('month', NOW())`),
      query(`SELECT b.*, i.nombre as insumo_nombre, i.unidad_medida,
                    emp.nombre as usuario_nombre
             FROM bitacora b
             JOIN insumos i ON b.insumo_id = i.id
             JOIN usuarios u ON b.usuario_id = u.id
             JOIN empleados emp ON u.empleado_id = emp.id
             ORDER BY b.fecha DESC LIMIT 8`)
    ]);

    // Insumos con menor stock
    const stockCritico = await query(
      `SELECT id, nombre, existencia, cantidad_minima, unidad_medida,
              ROUND((existencia / NULLIF(cantidad_minima, 0)) * 100, 1) as porcentaje_stock
       FROM insumos WHERE activo = true AND cantidad_minima > 0
       ORDER BY (existencia / NULLIF(cantidad_minima, 0))
       LIMIT 5`
    );

    // Tendencia entradas/salidas últimos 7 días
    const tendencia = await query(
      `WITH dias AS (
        SELECT generate_series(NOW() - INTERVAL '6 days', NOW(), '1 day')::date as dia
      )
      SELECT d.dia,
        COALESCE(SUM(CASE WHEN b.tipo = 'entrada' THEN b.cantidad ELSE 0 END), 0) as entradas,
        COALESCE(SUM(CASE WHEN b.tipo = 'salida' THEN b.cantidad ELSE 0 END), 0) as salidas
      FROM dias d
      LEFT JOIN bitacora b ON b.fecha::date = d.dia
      GROUP BY d.dia ORDER BY d.dia`
    );

    res.json({
      resumen: {
        total_insumos: parseInt(totalInsumos.rows[0].count),
        por_caducar: parseInt(porCaducar.rows[0].count),
        caducados: parseInt(caducados.rows[0].count),
        stock_bajo: parseInt(stockBajo.rows[0].count),
        pedidos_pendientes: parseInt(pedidosPendientes.rows[0].count),
        pedidos_almacen_pendientes: parseInt(pedidosAlmacenPendientes.rows[0].count),
        entradas_mes: parseFloat(entradasMes.rows[0].total),
        salidas_mes: parseFloat(salidasMes.rows[0].total)
      },
      stock_critico: stockCritico.rows,
      movimientos_recientes: movimientosRecientes.rows,
      tendencia: tendencia.rows
    });
  } catch (err) {
    next(err);
  }
};

module.exports = { getStats };
