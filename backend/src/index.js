require('dotenv').config();
const http = require('http');
const app = require('./app');
const { initSocket } = require('./socket');
const { testConnection } = require('./db/pool');
const logger = require('./utils/logger');

const PORT = process.env.PORT || 4000;

const server = http.createServer(app);
initSocket(server);

async function start() {
  try {
    await testConnection();
    logger.info('✅ PostgreSQL conectado correctamente');

    server.listen(PORT, () => {
      logger.info(`🚀 Servidor HMNG corriendo en puerto ${PORT}`);
      logger.info(`📡 Environment: ${process.env.NODE_ENV}`);
    });
  } catch (err) {
    logger.error('❌ Error al iniciar el servidor:', err);
    process.exit(1);
  }
}

start();
