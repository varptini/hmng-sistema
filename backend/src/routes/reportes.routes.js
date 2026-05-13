const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const { getInventario, getMovimientos } = require('../controllers/reportes.controller');

const router = Router();
router.use(authenticate);
router.get('/inventario', getInventario);
router.get('/movimientos', getMovimientos);

module.exports = router;
