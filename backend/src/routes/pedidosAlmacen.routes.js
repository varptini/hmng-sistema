const { Router } = require('express');
const { authenticate, authorize } = require('../middleware/auth');
const { getAll, getById, create, updateEstado } = require('../controllers/pedidosAlmacen.controller');

const router = Router();
router.use(authenticate);
router.get('/', getAll);
router.get('/:id', getById);
router.post('/', authorize('Administrador', 'Abastecedor'), create);
router.put('/:id/estado', authorize('Administrador', 'Abastecedor'), updateEstado);

module.exports = router;
