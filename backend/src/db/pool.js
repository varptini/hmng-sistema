const { Pool } = require('pg');
const logger = require('../utils/logger');

const pool = new Pool({
  connectionString: process.env.DATABASE_URL,
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

pool.on('error', (err) => {
  logger.error('Error inesperado en el pool de PostgreSQL:', err);
});

async function testConnection() {
  const client = await pool.connect();
  try {
    await client.query('SELECT NOW()');
    return true;
  } finally {
    client.release();
  }
}

async function query(text, params) {
  const start = Date.now();
  try {
    const res = await pool.query(text, params);
    const duration = Date.now() - start;
    if (duration > 1000) {
      logger.warn(`Query lenta (${duration}ms): ${text}`);
    }
    return res;
  } catch (err) {
    logger.error('Error en query:', { text, error: err.message });
    throw err;
  }
}

async function getClient() {
  return pool.connect();
}

module.exports = { query, getClient, testConnection, pool };
