const { Router } = require('express');
const { authenticate, authorize } = require('../middleware/auth');
const { getAll, getById, create } = require('../controllers/salidas.controller');

const router = Router();
router.use(authenticate);
router.get('/', getAll);
router.get('/:id', getById);
router.post('/', authorize('Administrador', 'Suministrador'), create);

module.exports = router;
