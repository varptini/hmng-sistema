const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');
const rateLimit = require('express-rate-limit');

const authRoutes = require('./routes/auth.routes');
const empleadosRoutes = require('./routes/empleados.routes');
const usuariosRoutes = require('./routes/usuarios.routes');
const rolesRoutes = require('./routes/roles.routes');
const insumosRoutes = require('./routes/insumos.routes');
const serviciosRoutes = require('./routes/servicios.routes');
const pedidosAlmacenRoutes = require('./routes/pedidosAlmacen.routes');
const pedidosSubalmacenRoutes = require('./routes/pedidosSubalmacen.routes');
const bitacoraRoutes = require('./routes/bitacora.routes');
const dashboardRoutes = require('./routes/dashboard.routes');
const notificacionesRoutes = require('./routes/notificaciones.routes');
const entradasRoutes = require('./routes/entradas.routes');
const salidasRoutes = require('./routes/salidas.routes');
const reportesRoutes = require('./routes/reportes.routes');

const { errorHandler } = require('./middleware/errorHandler');
const logger = require('./utils/logger');

const app = express();

app.use(helmet());
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:5173',
  credentials: true,
}));

const apiLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 200,
  message: { error: 'Demasiadas solicitudes, intenta más tarde.' }
});

// Rate limit estricto para login: previene ataques de fuerza bruta
const loginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 10,
  message: { error: 'Demasiados intentos de inicio de sesión. Intenta en 15 minutos.' }
});

app.use('/api/', apiLimiter);
app.use('/api/auth/login', loginLimiter);

app.use(morgan('combined', { stream: { write: (msg) => logger.info(msg.trim()) } }));
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

app.get('/health', (req, res) => {
  res.json({ status: 'OK', timestamp: new Date().toISOString(), service: 'HMNG API' });
});

app.use('/api/auth', authRoutes);
app.use('/api/empleados', empleadosRoutes);
app.use('/api/usuarios', usuariosRoutes);
app.use('/api/roles', rolesRoutes);
app.use('/api/insumos', insumosRoutes);
app.use('/api/servicios', serviciosRoutes);
app.use('/api/pedidos-almacen', pedidosAlmacenRoutes);
app.use('/api/pedidos-subalmacen', pedidosSubalmacenRoutes);
app.use('/api/bitacora', bitacoraRoutes);
app.use('/api/dashboard', dashboardRoutes);
app.use('/api/notificaciones', notificacionesRoutes);
app.use('/api/entradas', entradasRoutes);
app.use('/api/salidas', salidasRoutes);
app.use('/api/reportes', reportesRoutes);

app.use((req, res) => {
  res.status(404).json({ error: 'Endpoint no encontrado' });
});

app.use(errorHandler);

module.exports = app;
