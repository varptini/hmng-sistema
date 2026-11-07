const { Router } = require('express');
const { authenticate, authorize } = require('../middleware/auth');
const { getAll, create, update, remove } = require('../controllers/roles.controller');

const router = Router();
router.use(authenticate);
router.get('/', getAll);
router.post('/', authorize('Administrador'), create);
router.put('/:id', authorize('Administrador'), update);
router.delete('/:id', authorize('Administrador'), remove);

module.exports = router;
