const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/insumos.controller');
const { authenticate, authorize } = require('../middleware/auth');

router.use(authenticate);
router.get('/', ctrl.getAll);
router.get('/alertas', ctrl.getAlertas);
router.get('/:id', ctrl.getById);
router.post('/', authorize('Administrador', 'Abastecedor'), ctrl.create);
router.put('/:id', authorize('Administrador', 'Abastecedor', 'Suministrador'), ctrl.update);
router.delete('/:id', authorize('Administrador'), ctrl.remove);

module.exports = router;
