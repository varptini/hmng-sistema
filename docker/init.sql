-- ============================================================
-- HMNG - Hospital de la Madre y el Niño Guerrerense
-- Schema inicial de base de datos PostgreSQL
-- ============================================================

-- Extensiones
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- TABLA: roles
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE,
  descripcion TEXT,
  observaciones TEXT,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: empleados
-- ============================================================
CREATE TABLE IF NOT EXISTS empleados (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  direccion VARCHAR(150),
  telefono VARCHAR(20),
  correo VARCHAR(100),
  celular VARCHAR(20),
  fecha_nacimiento DATE,
  activo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: usuarios
-- ============================================================
CREATE TABLE IF NOT EXISTS usuarios (
  id SERIAL PRIMARY KEY,
  nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
  contrasena VARCHAR(255) NOT NULL,
  activo BOOLEAN DEFAULT TRUE,
  empleado_id INTEGER NOT NULL REFERENCES empleados(id) ON DELETE CASCADE,
  rol_id INTEGER NOT NULL REFERENCES roles(id),
  ultimo_acceso TIMESTAMP,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: servicios
-- ============================================================
CREATE TABLE IF NOT EXISTS servicios (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  descripcion TEXT,
  activo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: responsables_servicio
-- ============================================================
CREATE TABLE IF NOT EXISTS responsables_servicio (
  id SERIAL PRIMARY KEY,
  usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
  servicio_id INTEGER NOT NULL REFERENCES servicios(id),
  activo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: insumos
-- ============================================================
CREATE TABLE IF NOT EXISTS insumos (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  descripcion TEXT,
  unidad_medida VARCHAR(30) NOT NULL,
  existencia DECIMAL(10,2) DEFAULT 0,
  cantidad_minima DECIMAL(10,2) DEFAULT 0,
  lote VARCHAR(100),
  fecha_caducidad DATE,
  codigo_barras VARCHAR(100),
  activo BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: almacen_general (proveedor externo)
-- ============================================================
CREATE TABLE IF NOT EXISTS almacen_general (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  direccion VARCHAR(200),
  email VARCHAR(100),
  telefono VARCHAR(20),
  descripcion TEXT,
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: entradas (abastecimiento del sub-almacén)
-- ============================================================
CREATE TABLE IF NOT EXISTS entradas (
  id SERIAL PRIMARY KEY,
  fecha_registro TIMESTAMP DEFAULT NOW(),
  observaciones TEXT,
  usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
  almacen_general_id INTEGER REFERENCES almacen_general(id),
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: detalle_entradas
-- ============================================================
CREATE TABLE IF NOT EXISTS detalle_entradas (
  id SERIAL PRIMARY KEY,
  entrada_id INTEGER NOT NULL REFERENCES entradas(id) ON DELETE CASCADE,
  insumo_id INTEGER NOT NULL REFERENCES insumos(id),
  cantidad DECIMAL(10,2) NOT NULL,
  lote VARCHAR(100),
  fecha_caducidad DATE,
  precio_unitario DECIMAL(10,2),
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: salidas (distribución a servicios)
-- ============================================================
CREATE TABLE IF NOT EXISTS salidas (
  id SERIAL PRIMARY KEY,
  fecha_registro TIMESTAMP DEFAULT NOW(),
  observaciones TEXT,
  usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
  servicio_id INTEGER NOT NULL REFERENCES servicios(id),
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: detalle_salidas
-- ============================================================
CREATE TABLE IF NOT EXISTS detalle_salidas (
  id SERIAL PRIMARY KEY,
  salida_id INTEGER NOT NULL REFERENCES salidas(id) ON DELETE CASCADE,
  insumo_id INTEGER NOT NULL REFERENCES insumos(id),
  cantidad DECIMAL(10,2) NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: pedidos_almacen_general (abastecedor → almacén central)
-- ============================================================
CREATE TABLE IF NOT EXISTS pedidos_almacen_general (
  id SERIAL PRIMARY KEY,
  estado VARCHAR(20) DEFAULT 'pendiente' CHECK (estado IN ('pendiente','enviado','recibido','cancelado')),
  fecha_pedido TIMESTAMP DEFAULT NOW(),
  fecha_recepcion TIMESTAMP,
  observaciones TEXT,
  usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
  almacen_general_id INTEGER REFERENCES almacen_general(id),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: detalle_pedidos_almacen
-- ============================================================
CREATE TABLE IF NOT EXISTS detalle_pedidos_almacen (
  id SERIAL PRIMARY KEY,
  pedido_almacen_id INTEGER NOT NULL REFERENCES pedidos_almacen_general(id) ON DELETE CASCADE,
  insumo_id INTEGER NOT NULL REFERENCES insumos(id),
  cantidad_solicitada DECIMAL(10,2) NOT NULL,
  cantidad_entregada DECIMAL(10,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: pedidos_subalmacen (servicio → sub-almacén)
-- ============================================================
CREATE TABLE IF NOT EXISTS pedidos_subalmacen (
  id SERIAL PRIMARY KEY,
  estado VARCHAR(20) DEFAULT 'pendiente' CHECK (estado IN ('pendiente','atendido','cancelado')),
  fecha_pedido TIMESTAMP DEFAULT NOW(),
  fecha_atencion TIMESTAMP,
  observaciones TEXT,
  usuario_solicitante_id INTEGER NOT NULL REFERENCES usuarios(id),
  usuario_atiende_id INTEGER REFERENCES usuarios(id),
  servicio_id INTEGER NOT NULL REFERENCES servicios(id),
  created_at TIMESTAMP DEFAULT NOW(),
  updated_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: detalle_pedidos_subalmacen
-- ============================================================
CREATE TABLE IF NOT EXISTS detalle_pedidos_subalmacen (
  id SERIAL PRIMARY KEY,
  pedido_subalmacen_id INTEGER NOT NULL REFERENCES pedidos_subalmacen(id) ON DELETE CASCADE,
  insumo_id INTEGER NOT NULL REFERENCES insumos(id),
  cantidad_solicitada DECIMAL(10,2) NOT NULL,
  cantidad_surtida DECIMAL(10,2) DEFAULT 0,
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: bitacora
-- ============================================================
CREATE TABLE IF NOT EXISTS bitacora (
  id SERIAL PRIMARY KEY,
  tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('entrada','salida')),
  insumo_id INTEGER NOT NULL REFERENCES insumos(id),
  cantidad DECIMAL(10,2) NOT NULL,
  existencia_anterior DECIMAL(10,2),
  existencia_nueva DECIMAL(10,2),
  referencia_id INTEGER,
  referencia_tipo VARCHAR(50),
  usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
  fecha TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: notificaciones
-- ============================================================
CREATE TABLE IF NOT EXISTS notificaciones (
  id SERIAL PRIMARY KEY,
  usuario_id INTEGER NOT NULL REFERENCES usuarios(id),
  tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('caducidad','stock_minimo','pedido_nuevo','pedido_atendido')),
  mensaje TEXT NOT NULL,
  leida BOOLEAN DEFAULT FALSE,
  referencia_id INTEGER,
  created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- TABLA: auditoria
-- ============================================================
CREATE TABLE IF NOT EXISTS auditoria (
  id SERIAL PRIMARY KEY,
  usuario_id INTEGER REFERENCES usuarios(id),
  accion VARCHAR(100) NOT NULL,
  tabla_afectada VARCHAR(50),
  descripcion TEXT,
  ip_address VARCHAR(45),
  fecha TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- DATOS INICIALES
-- ============================================================

-- Roles del sistema
INSERT INTO roles (nombre, descripcion) VALUES
  ('Administrador', 'Acceso total al sistema'),
  ('Abastecedor', 'Gestión de insumos y pedidos al almacén general'),
  ('Suministrador', 'Distribución de insumos a servicios'),
  ('Responsable de Servicio', 'Realiza pedidos al sub-almacén')
ON CONFLICT (nombre) DO NOTHING;

-- Servicios hospitalarios
INSERT INTO servicios (nombre, descripcion) VALUES
  ('UCIN', 'Unidad de Cuidados Intensivos Neonatales'),
  ('UTIN', 'Unidad de Terapia Intensiva Neonatal'),
  ('UCIA', 'Unidad de Cuidados Intensivos Adultos'),
  ('CEYE', 'Central de Equipo y Esterilización'),
  ('Hospitalización', 'Área de hospitalización general'),
  ('Urgencias', 'Servicio de urgencias'),
  ('Quirófano', 'Área de quirófanos'),
  ('Tococirugía', 'Servicio de tococirugía')
ON CONFLICT DO NOTHING;

-- Almacén general
INSERT INTO almacen_general (nombre, direccion, email, telefono, descripcion) VALUES
  ('Almacén Central HMNG', 'Chilpancingo, Guerrero', 'almacen@hmng.gob.mx', '7471234567', 'Almacén central de insumos del hospital')
ON CONFLICT DO NOTHING;

-- Empleado administrador inicial
INSERT INTO empleados (nombre, correo, telefono) VALUES
  ('Administrador Sistema', 'admin@hmng.gob.mx', '7470000000')
ON CONFLICT DO NOTHING;

-- Usuario admin (contraseña: Admin2025! — hasheada con bcrypt rounds=12)
-- Hash pre-generado para no requerir bcrypt en init.sql
INSERT INTO usuarios (nombre_usuario, contrasena, empleado_id, rol_id) VALUES
  ('admin', '$2a$12$KL7j2zxevGtw7myu86E8QuwfMPOOan8aqgR30Jjn6TUE0DVyysDXW', 1, 1)
ON CONFLICT (nombre_usuario) DO NOTHING;

-- Insumos de ejemplo
INSERT INTO insumos (nombre, descripcion, unidad_medida, existencia, cantidad_minima, lote, fecha_caducidad) VALUES
  ('Guantes de látex talla M', 'Guantes desechables para procedimientos', 'Caja x100', 50, 10, 'L2025-001', '2026-06-01'),
  ('Jeringa 10ml con aguja', 'Jeringa desechable estéril', 'Pieza', 200, 50, 'L2025-002', '2027-01-01'),
  ('Gasa estéril 10x10', 'Compresa de gasa esterilizada', 'Paquete x10', 150, 30, 'L2025-003', '2026-12-01'),
  ('Solución salina 0.9% 1L', 'Solución para infusión IV', 'Bolsa 1L', 80, 20, 'L2025-004', '2025-08-01'),
  ('Alcohol al 70%', 'Alcohol isopropílico para antisepsia', 'Litro', 40, 15, 'L2025-005', '2026-09-01'),
  ('Mascarilla quirúrgica', 'Mascarilla desechable tricapa', 'Caja x50', 30, 10, 'L2025-006', '2026-03-01'),
  ('Catéter IV 18G', 'Catéter intravenoso calibre 18', 'Pieza', 100, 25, 'L2025-007', '2027-03-01'),
  ('Cinta adhesiva médica', 'Micropore 1.25cm x 9.1m', 'Rollo', 60, 15, 'L2025-008', '2028-01-01')
ON CONFLICT DO NOTHING;

-- ============================================================
-- ÍNDICES para optimizar consultas frecuentes
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_insumos_nombre ON insumos(nombre);
CREATE INDEX IF NOT EXISTS idx_insumos_caducidad ON insumos(fecha_caducidad);
CREATE INDEX IF NOT EXISTS idx_insumos_existencia ON insumos(existencia);
CREATE INDEX IF NOT EXISTS idx_bitacora_insumo ON bitacora(insumo_id);
CREATE INDEX IF NOT EXISTS idx_bitacora_fecha ON bitacora(fecha);
CREATE INDEX IF NOT EXISTS idx_notificaciones_usuario ON notificaciones(usuario_id, leida);
CREATE INDEX IF NOT EXISTS idx_pedidos_estado ON pedidos_subalmacen(estado);
CREATE INDEX IF NOT EXISTS idx_usuarios_nombre ON usuarios(nombre_usuario);

-- ============================================================
-- FUNCIÓN para actualizar updated_at automáticamente
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers de updated_at
CREATE TRIGGER update_insumos_updated_at BEFORE UPDATE ON insumos FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_usuarios_updated_at BEFORE UPDATE ON usuarios FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_pedidos_subalmacen_updated_at BEFORE UPDATE ON pedidos_subalmacen FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_pedidos_almacen_updated_at BEFORE UPDATE ON pedidos_almacen_general FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
