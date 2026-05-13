// usuarios.routes.js
const express = require('express');
const r1 = express.Router();
const uctrl = require('../controllers/usuarios.controller');
const { authenticate, authorize } = require('../middleware/auth');
r1.use(authenticate);
r1.get('/', authorize('Administrador'), uctrl.getAll);
r1.get('/:id', authorize('Administrador'), uctrl.getById);
r1.post('/', authorize('Administrador'), uctrl.create);
r1.put('/:id', authorize('Administrador'), uctrl.update);
r1.delete('/:id', authorize('Administrador'), uctrl.remove);
module.exports = r1;
