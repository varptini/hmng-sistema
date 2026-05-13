const logger = require('../utils/logger');

const errorHandler = (err, req, res, next) => {
  logger.error('Error no manejado:', {
    message: err.message,
    stack: err.stack,
    url: req.url,
    method: req.method,
    user: req.user?.id
  });

  // Errores de PostgreSQL
  if (err.code === '23505') {
    return res.status(409).json({ error: 'El registro ya existe (dato duplicado)' });
  }
  if (err.code === '23503') {
    return res.status(400).json({ error: 'Referencia inválida a otro registro' });
  }
  if (err.code === '23502') {
    return res.status(400).json({ error: 'Campo requerido faltante' });
  }

  const status = err.status || 500;
  const message = process.env.NODE_ENV === 'production'
    ? 'Error interno del servidor'
    : err.message;

  res.status(status).json({ error: message });
};

module.exports = { errorHandler };
