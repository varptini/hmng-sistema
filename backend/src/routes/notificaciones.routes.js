const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const { getAll, marcarLeidas } = require('../controllers/notificaciones.controller');

const router = Router();
router.use(authenticate);
router.get('/', getAll);
router.put('/marcar-leidas', marcarLeidas);

module.exports = router;
