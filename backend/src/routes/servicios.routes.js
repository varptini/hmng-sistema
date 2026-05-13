const { Router } = require('express');
const { authenticate, authorize } = require('../middleware/auth');
const { getAll, create, update } = require('../controllers/servicios.controller');

const router = Router();
router.use(authenticate);
router.get('/', getAll);
router.post('/', authorize('Administrador'), create);
router.put('/:id', authorize('Administrador'), update);

module.exports = router;
