const express = require('express');
const router = express.Router();
const ctrl = require('../controllers/entradas.controller');
const { authenticate, authorize } = require('../middleware/auth');
router.use(authenticate);
router.get('/', ctrl.getAll);
router.get('/:id', ctrl.getById);
router.post('/', authorize('Administrador','Abastecedor'), ctrl.create);
module.exports = router;
