const { Router } = require('express');
const { authenticate, authorize } = require('../middleware/auth');
const { getAll, create, update, remove } = require('../controllers/empleados.controller');

const router = Router();
router.use(authenticate, authorize('Administrador'));
router.get('/', getAll);
router.post('/', create);
router.put('/:id', update);
router.delete('/:id', remove);

module.exports = router;
