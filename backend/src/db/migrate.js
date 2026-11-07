require('dotenv').config();
const fs = require('fs');
const path = require('path');
const { Pool } = require('pg');
const logger = require('../utils/logger');

const INIT_SQL_PATH = path.join(__dirname, '../../../docker/init.sql');

function readSchemaSql() {
  const sql = fs.readFileSync(INIT_SQL_PATH, 'utf8');
  const datosIdx = sql.indexOf('-- DATOS INICIALES');
  const indicesIdx = sql.indexOf('-- ÍNDICES');

  if (datosIdx === -1 || indicesIdx === -1) {
    throw new Error('No se encontraron los marcadores esperados en docker/init.sql');
  }

  // Esquema (tablas) + índices, sin los INSERT de datos iniciales (eso es seed.js)
  return sql.slice(0, datosIdx) + sql.slice(indicesIdx);
}

async function migrate() {
  const pool = new Pool({ connectionString: process.env.DATABASE_URL });
  try {
    await pool.query(readSchemaSql());
    logger.info('✅ Migración completada: esquema creado/actualizado.');
  } finally {
    await pool.end();
  }
}

migrate().catch((err) => {
  logger.error('❌ Error en migración:', err);
  process.exit(1);
});
