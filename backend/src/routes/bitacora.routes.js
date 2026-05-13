const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const { getAll } = require('../controllers/bitacora.controller');

const router = Router();
router.use(authenticate);
router.get('/', getAll);

module.exports = router;
