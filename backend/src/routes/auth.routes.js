// auth.routes.js
const express = require('express');
const router = express.Router();
const { login, getProfile, cambiarContrasena } = require('../controllers/auth.controller');
const { authenticate } = require('../middleware/auth');

router.post('/login', login);
router.get('/profile', authenticate, getProfile);
router.put('/cambiar-contrasena', authenticate, cambiarContrasena);

module.exports = router;
