const { query } = require('../db/pool');

async function registrarAuditoria(usuario_id, accion, tabla_afectada, descripcion) {
  try {
    await query(
      `INSERT INTO auditoria (usuario_id, accion, tabla_afectada, descripcion)
       VALUES ($1, $2, $3, $4)`,
      [usuario_id, accion, tabla_afectada, descripcion]
    );
  } catch (e) {
    // No crítico — no interrumpir el flujo
  }
}

module.exports = { registrarAuditoria };
