const { Server } = require('socket.io');
const jwt = require('jsonwebtoken');
const logger = require('./utils/logger');

let io;
const userSockets = new Map(); // userId -> socketId

function initSocket(server) {
  io = new Server(server, {
    cors: {
      origin: process.env.FRONTEND_URL || 'http://localhost:5173',
      methods: ['GET', 'POST'],
      credentials: true
    }
  });

  io.use((socket, next) => {
    const token = socket.handshake.auth.token;
    if (!token) return next(new Error('Token requerido'));

    try {
      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      socket.userId = decoded.id;
      next();
    } catch (err) {
      next(new Error('Token inválido'));
    }
  });

  io.on('connection', (socket) => {
    logger.info(`Socket conectado: usuario ${socket.userId}`);
    userSockets.set(socket.userId, socket.id);

    socket.on('disconnect', () => {
      userSockets.delete(socket.userId);
      logger.info(`Socket desconectado: usuario ${socket.userId}`);
    });
  });

  logger.info('✅ Socket.io inicializado');
}

function emitirNotificacion(userId, tipo, data) {
  if (!io) return;

  if (userId) {
    const socketId = userSockets.get(userId);
    if (socketId) {
      io.to(socketId).emit('notificacion', { tipo, data, timestamp: new Date() });
    }
  } else {
    // Broadcast a todos
    io.emit('notificacion', { tipo, data, timestamp: new Date() });
  }
}

module.exports = { initSocket, emitirNotificacion };
