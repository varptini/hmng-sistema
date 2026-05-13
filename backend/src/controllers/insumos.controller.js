const { query, getClient } = require('../db/pool');
const { registrarAuditoria } = require('../utils/auditoria');
const { notificarStockBajo } = require('../services/notificationService');

const INSUMO_COLUMNS = `
  i.id, i.nombre, i.descripcion, i.unidad_medida, i.existencia, i.cantidad_minima,
  i.lote, i.fecha_caducidad, i.codigo_barras, i.activo, i.created_at, i.updated_at,
  CASE WHEN i.fecha_caducidad < NOW() THEN 'caducado'
       WHEN i.fecha_caducidad <= NOW() + INTERVAL '30 days' THEN 'por_caducar'
       ELSE 'vigente' END AS estado_caducidad,
  CASE WHEN i.existencia = 0 THEN 'agotado'
       WHEN i.existencia <= i.cantidad_minima THEN 'stock_bajo'
       ELSE 'normal' END AS estado_stock
`;

/** Escapa caracteres especiales de LIKE para búsquedas literales */
function escapeLike(text) {
  return text.replace(/[%_\\]/g, '\\$&');
}

/** Normaliza y limita parámetros de paginación */
function buildPagination(page, limit) {
  const safePage = Math.max(1, parseInt(page) || 1);
  const safeLimit = Math.min(Math.max(1, parseInt(limit) || 20), 100);
  return { safeLimit, offset: (safePage - 1) * safeLimit, safePage };
}

const getAll = async (req, res, next) => {
  try {
    const { search, alerta, page, limit } = req.query;
    const { safeLimit, offset, safePage } = buildPagination(page, limit);

    const whereConditions = ['i.activo = true'];
    const params = [];

    if (search) {
      const escaped = escapeLike(search);
      params.push(`%${escaped}%`);
      whereConditions.push(`(i.nombre ILIKE $${params.length} OR i.descripcion ILIKE $${params.length})`);
    }

    if (alerta === 'caducidad') {
      whereConditions.push(`i.fecha_caducidad <= NOW() + INTERVAL '30 days' AND i.fecha_caducidad >= NOW()`);
    } else if (alerta === 'stock') {
      whereConditions.push(`i.existencia <= i.cantidad_minima`);
    } else if (alerta === 'caducado') {
      whereConditions.push(`i.fecha_caducidad < NOW()`);
    }

    const where = 'WHERE ' + whereConditions.join(' AND ');
    params.push(safeLimit, offset);

    const [result, countResult] = await Promise.all([
      query(
        `SELECT ${INSUMO_COLUMNS} FROM insumos i ${where}
         ORDER BY i.nombre
         LIMIT $${params.length - 1} OFFSET $${params.length}`,
        params
      ),
      query(
        `SELECT COUNT(*) FROM insumos i ${where}`,
        params.slice(0, -2)
      )
    ]);

    res.json({
      data: result.rows,
      total: parseInt(countResult.rows[0].count),
      page: safePage,
      limit: safeLimit
    });
  } catch (err) {
    next(err);
  }
};

const getById = async (req, res, next) => {
  try {
    const result = await query(
      `SELECT ${INSUMO_COLUMNS} FROM insumos i WHERE i.id = $1 AND i.activo = true`,
      [req.params.id]
    );

    if (!result.rows.length) {
      return res.status(404).json({ error: 'Insumo no encontrado' });
    }

    res.json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const create = async (req, res, next) => {
  try {
    const { nombre, descripcion, unidad_medida, existencia = 0, cantidad_minima = 0, lote, fecha_caducidad, codigo_barras } = req.body;

    if (!nombre || !unidad_medida) {
      return res.status(400).json({ error: 'Nombre y unidad de medida son requeridos' });
    }

    const result = await query(
      `INSERT INTO insumos (nombre, descripcion, unidad_medida, existencia, cantidad_minima, lote, fecha_caducidad, codigo_barras)
       VALUES ($1,$2,$3,$4,$5,$6,$7,$8) RETURNING *`,
      [nombre, descripcion, unidad_medida, existencia, cantidad_minima, lote, fecha_caducidad, codigo_barras]
    );

    await registrarAuditoria(req.user.id, 'CREAR_INSUMO', 'insumos', `Insumo creado: ${nombre}`);

    if (existencia <= cantidad_minima && cantidad_minima > 0) {
      await notificarStockBajo(result.rows[0]);
    }

    res.status(201).json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const update = async (req, res, next) => {
  try {
    const { nombre, descripcion, unidad_medida, cantidad_minima, lote, fecha_caducidad, codigo_barras } = req.body;

    const result = await query(
      `UPDATE insumos SET
        nombre = COALESCE($1, nombre),
        descripcion = COALESCE($2, descripcion),
        unidad_medida = COALESCE($3, unidad_medida),
        cantidad_minima = COALESCE($4, cantidad_minima),
        lote = COALESCE($5, lote),
        fecha_caducidad = COALESCE($6, fecha_caducidad),
        codigo_barras = COALESCE($7, codigo_barras)
       WHERE id = $8 AND activo = true RETURNING *`,
      [nombre, descripcion, unidad_medida, cantidad_minima, lote, fecha_caducidad, codigo_barras, req.params.id]
    );

    if (!result.rows.length) {
      return res.status(404).json({ error: 'Insumo no encontrado' });
    }

    await registrarAuditoria(req.user.id, 'ACTUALIZAR_INSUMO', 'insumos', `Insumo actualizado: ${result.rows[0].nombre}`);
    res.json(result.rows[0]);
  } catch (err) {
    next(err);
  }
};

const remove = async (req, res, next) => {
  try {
    const result = await query(
      `UPDATE insumos SET activo = false WHERE id = $1 AND activo = true RETURNING nombre`,
      [req.params.id]
    );

    if (!result.rows.length) {
      return res.status(404).json({ error: 'Insumo no encontrado' });
    }

    await registrarAuditoria(req.user.id, 'ELIMINAR_INSUMO', 'insumos', `Insumo eliminado: ${result.rows[0].nombre}`);
    res.json({ message: 'Insumo eliminado correctamente' });
  } catch (err) {
    next(err);
  }
};

const getAlertas = async (req, res, next) => {
  try {
    const [porCaducar, stockBajo, caducados] = await Promise.all([
      query(
        `SELECT id, nombre, unidad_medida, existencia, fecha_caducidad FROM insumos
         WHERE activo = true AND fecha_caducidad <= NOW() + INTERVAL '30 days' AND fecha_caducidad > NOW()
         ORDER BY fecha_caducidad`
      ),
      query(
        `SELECT id, nombre, unidad_medida, existencia, cantidad_minima FROM insumos
         WHERE activo = true AND existencia <= cantidad_minima AND existencia > 0
         ORDER BY existencia`
      ),
      query(
        `SELECT id, nombre, unidad_medida, existencia, fecha_caducidad FROM insumos
         WHERE activo = true AND fecha_caducidad < NOW()
         ORDER BY fecha_caducidad`
      )
    ]);

    res.json({
      por_caducar: porCaducar.rows,
      stock_bajo: stockBajo.rows,
      caducados: caducados.rows,
      totales: {
        por_caducar: porCaducar.rows.length,
        stock_bajo: stockBajo.rows.length,
        caducados: caducados.rows.length
      }
    });
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll, getById, create, update, remove, getAlertas };
