const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const { getAll } = require('../controllers/salidas.controller');

const router = Router();
router.use(authenticate);
router.get('/', getAll);

module.exports = router;
