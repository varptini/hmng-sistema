const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const { getStats } = require('../controllers/dashboard.controller');

const router = Router();
router.use(authenticate);
router.get('/', getStats);

module.exports = router;
