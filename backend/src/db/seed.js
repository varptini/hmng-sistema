require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { Pool } = require('pg');
const logger = require('../utils/logger');

const INIT_SQL_PATH = path.join(__dirname, '../../../docker/init.sql');

function readSeedSql() {
  const sql = fs.readFileSync(INIT_SQL_PATH, 'utf8');
  const datosIdx = sql.indexOf('-- DATOS INICIALES');
  const indicesIdx = sql.indexOf('-- ÍNDICES');

  if (datosIdx === -1 || indicesIdx === -1) {
    throw new Error('No se encontraron los marcadores esperados en docker/init.sql');
  }

  return sql.slice(datosIdx, indicesIdx);
}

async function seed() {
  const pool = new Pool({ connectionString: process.env.DATABASE_URL });
  try {
    // Varias de las tablas de datos iniciales (servicios, insumos, almacen_general)
    // no tienen una restricción UNIQUE en nombre, así que el ON CONFLICT DO NOTHING
    // del propio SQL no evita duplicados si el seed ya corrió antes (p. ej. vía
    // docker-entrypoint-initdb.d). Evitamos reinsertar si ya hay usuarios.
    const { rows } = await pool.query('SELECT COUNT(*) FROM usuarios');
    if (parseInt(rows[0].count) > 0) {
      logger.info('⏭️  Seed omitido: ya existen usuarios en la base de datos.');
      return;
    }

    await pool.query(readSeedSql());
    logger.info('✅ Seed completado: datos iniciales insertados (usuario admin / Admin2025!).');
  } finally {
    await pool.end();
  }
}

seed().catch((err) => {
  logger.error('❌ Error en seed:', err);
  process.exit(1);
});
