-- =====================================================================
-- KioscSys - Sistema de gestión de stock, costos y ventas
-- Kiosco Don Mateo
-- Script de creación de la base de datos, carga de datos y consultas
-- Motor: MySQL 8
-- =====================================================================

SET NAMES utf8mb4;
DROP DATABASE IF EXISTS kioscsys;
CREATE DATABASE kioscsys CHARACTER SET utf8mb4 COLLATE utf8mb4_spanish_ci;
USE kioscsys;

-- ---------------------------------------------------------------------
-- 1. CREACIÓN DE TABLAS
-- ---------------------------------------------------------------------

CREATE TABLE rol (
  id_rol      INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE usuario (
  id_usuario    INT AUTO_INCREMENT PRIMARY KEY,
  nombre        VARCHAR(50)  NOT NULL,
  apellido      VARCHAR(50)  NOT NULL,
  username      VARCHAR(30)  NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  id_rol        INT          NOT NULL,
  activo        BOOLEAN      NOT NULL DEFAULT TRUE,
  FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE categoria (
  id_categoria INT AUTO_INCREMENT PRIMARY KEY,
  nombre       VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

CREATE TABLE producto (
  id_producto    INT AUTO_INCREMENT PRIMARY KEY,
  codigo_barras  VARCHAR(20)   NOT NULL UNIQUE,
  nombre         VARCHAR(100)  NOT NULL,
  id_categoria   INT           NOT NULL,
  costo_unitario DECIMAL(10,2) NOT NULL CHECK (costo_unitario >= 0),
  precio_venta   DECIMAL(10,2) NOT NULL CHECK (precio_venta >= 0),
  stock_actual   INT           NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
  stock_minimo   INT           NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
  activo         BOOLEAN       NOT NULL DEFAULT TRUE,
  FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE proveedor (
  id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
  razon_social VARCHAR(100) NOT NULL,
  cuit         VARCHAR(13)  NOT NULL UNIQUE,
  telefono     VARCHAR(20)
) ENGINE=InnoDB;

CREATE TABLE compra (
  id_compra    INT AUTO_INCREMENT PRIMARY KEY,
  fecha        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_proveedor INT      NOT NULL,
  id_usuario   INT      NOT NULL,
  FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor),
  FOREIGN KEY (id_usuario)   REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- El costo se guarda por línea porque es el costo pactado en ESA compra
-- (dato histórico), no el costo vigente del producto.
CREATE TABLE detalle_compra (
  id_compra      INT           NOT NULL,
  id_producto    INT           NOT NULL,
  cantidad       INT           NOT NULL CHECK (cantidad > 0),
  costo_unitario DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id_compra, id_producto),
  FOREIGN KEY (id_compra)   REFERENCES compra(id_compra) ON DELETE CASCADE,
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB;

CREATE TABLE venta (
  id_venta   INT AUTO_INCREMENT PRIMARY KEY,
  fecha      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_usuario INT      NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- Precio y costo al momento de la venta: permiten calcular la
-- rentabilidad real aunque luego cambien los valores del producto.
CREATE TABLE detalle_venta (
  id_venta        INT           NOT NULL,
  id_producto     INT           NOT NULL,
  cantidad        INT           NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(10,2) NOT NULL,
  costo_unitario  DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (id_venta, id_producto),
  FOREIGN KEY (id_venta)    REFERENCES venta(id_venta) ON DELETE CASCADE,
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB;

CREATE TABLE conteo_fisico (
  id_conteo  INT AUTO_INCREMENT PRIMARY KEY,
  fecha      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_usuario INT      NOT NULL,
  FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- La diferencia no se almacena: se calcula (cantidad_contada - stock_sistema).
CREATE TABLE detalle_conteo (
  id_conteo        INT NOT NULL,
  id_producto      INT NOT NULL,
  stock_sistema    INT NOT NULL,
  cantidad_contada INT NOT NULL CHECK (cantidad_contada >= 0),
  PRIMARY KEY (id_conteo, id_producto),
  FOREIGN KEY (id_conteo)   REFERENCES conteo_fisico(id_conteo) ON DELETE CASCADE,
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
) ENGINE=InnoDB;

CREATE TABLE motivo_ajuste (
  id_motivo   INT AUTO_INCREMENT PRIMARY KEY,
  descripcion VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- cantidad con signo: negativa = baja de stock, positiva = alta.
-- id_conteo es opcional: el ajuste puede originarse o no en un conteo.
CREATE TABLE ajuste_inventario (
  id_ajuste   INT AUTO_INCREMENT PRIMARY KEY,
  fecha       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_producto INT          NOT NULL,
  id_usuario  INT          NOT NULL,
  id_motivo   INT          NOT NULL,
  id_conteo   INT          NULL,
  cantidad    INT          NOT NULL,
  observacion VARCHAR(200),
  FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
  FOREIGN KEY (id_usuario)  REFERENCES usuario(id_usuario),
  FOREIGN KEY (id_motivo)   REFERENCES motivo_ajuste(id_motivo),
  FOREIGN KEY (id_conteo)   REFERENCES conteo_fisico(id_conteo)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. CARGA DE DATOS DE EJEMPLO (INSERT)
-- ---------------------------------------------------------------------

INSERT INTO rol (nombre) VALUES ('Dueño/Administrador'), ('Vendedor');

INSERT INTO usuario (nombre, apellido, username, password_hash, id_rol) VALUES
  ('Mateo',  'Gómez', 'mgomez', SHA2('admin123', 256), 1),
  ('Lucía',  'Pérez', 'lperez', SHA2('venta123', 256), 2),
  ('Tomás',  'Ruiz',  'truiz',  SHA2('venta456', 256), 2);

INSERT INTO categoria (nombre) VALUES
  ('Golosinas'), ('Bebidas'), ('Cigarrillos'), ('Snacks'), ('Almacén');

INSERT INTO producto (codigo_barras, nombre, id_categoria, costo_unitario, precio_venta, stock_actual, stock_minimo) VALUES
  ('7790580100017', 'Alfajor triple chocolate',  1,  450.00,  800.00, 40, 15),
  ('7790895000997', 'Gaseosa cola 500 ml',       2,  900.00, 1500.00, 24, 12),
  ('7790040613707', 'Agua mineral 500 ml',       2,  500.00,  900.00,  8, 10),
  ('7791234000011', 'Cigarrillos box 20',        3, 2800.00, 3500.00, 30, 10),
  ('7790310981229', 'Papas fritas 85 g',         4,  850.00, 1400.00, 18,  8),
  ('7790070411311', 'Yerba mate 500 g',          5, 1900.00, 2900.00,  5,  6);

INSERT INTO proveedor (razon_social, cuit, telefono) VALUES
  ('Distribuidora Centro S.A.', '30-71234567-8', '351-4455667'),
  ('Mayorista Norte S.R.L.',    '30-70987654-3', '351-4998877');

INSERT INTO motivo_ajuste (descripcion) VALUES
  ('Rotura'), ('Vencimiento'), ('Error de carga'), ('Faltante'), ('Otro');

-- Una compra con su detalle
INSERT INTO compra (fecha, id_proveedor, id_usuario) VALUES ('2026-09-21 09:30:00', 1, 1);
INSERT INTO detalle_compra VALUES
  (1, 1, 24,  450.00),
  (1, 2, 12,  900.00);

-- Dos ventas con su detalle
INSERT INTO venta (fecha, id_usuario) VALUES
  ('2026-09-22 10:15:00', 2),
  ('2026-09-22 18:40:00', 3);
INSERT INTO detalle_venta VALUES
  (1, 1, 2,  800.00,  450.00),
  (1, 2, 1, 1500.00,  900.00),
  (2, 4, 1, 3500.00, 2800.00),
  (2, 5, 2, 1400.00,  850.00);

-- Un conteo físico con una discrepancia (papas fritas) y su ajuste
INSERT INTO conteo_fisico (fecha, id_usuario) VALUES ('2026-09-25 21:00:00', 2);
INSERT INTO detalle_conteo VALUES
  (1, 1, 40, 40),
  (1, 5, 18, 16);
INSERT INTO ajuste_inventario (id_producto, id_usuario, id_motivo, id_conteo, cantidad, observacion)
  VALUES (5, 1, 1, 1, -2, 'Dos paquetes rotos encontrados en depósito');
UPDATE producto SET stock_actual = stock_actual - 2 WHERE id_producto = 5;

-- ---------------------------------------------------------------------
-- 3. CONSULTAS (SELECT)
-- ---------------------------------------------------------------------

-- 3.1 Valorización del inventario por producto (RF-04)
SELECT p.nombre,
       p.stock_actual,
       p.stock_actual * p.costo_unitario AS valor_costo,
       p.stock_actual * p.precio_venta   AS valor_venta,
       p.stock_actual * (p.precio_venta - p.costo_unitario) AS margen_potencial
FROM producto p
WHERE p.activo = TRUE
ORDER BY valor_costo DESC;

-- 3.2 Valorización total del inventario (RF-04)
SELECT SUM(stock_actual * costo_unitario) AS total_costo,
       SUM(stock_actual * precio_venta)   AS total_venta,
       SUM(stock_actual * (precio_venta - costo_unitario)) AS margen_total
FROM producto
WHERE activo = TRUE;

-- 3.3 Alertas: productos por debajo del stock mínimo (RF-10)
SELECT p.nombre, c.nombre AS categoria, p.stock_actual, p.stock_minimo
FROM producto p
JOIN categoria c ON c.id_categoria = p.id_categoria
WHERE p.stock_actual < p.stock_minimo;

-- 3.4 Ranking de productos más vendidos con su ganancia real (RF-09)
SELECT p.nombre,
       SUM(dv.cantidad) AS unidades_vendidas,
       SUM(dv.cantidad * dv.precio_unitario) AS total_vendido,
       SUM(dv.cantidad * (dv.precio_unitario - dv.costo_unitario)) AS ganancia
FROM detalle_venta dv
JOIN producto p ON p.id_producto = dv.id_producto
GROUP BY p.id_producto, p.nombre
ORDER BY unidades_vendidas DESC;

-- 3.5 Ventas por vendedor y día (RF-08)
SELECT DATE(v.fecha) AS dia,
       CONCAT(u.nombre, ' ', u.apellido) AS vendedor,
       SUM(dv.cantidad * dv.precio_unitario) AS total
FROM venta v
JOIN usuario u        ON u.id_usuario = v.id_usuario
JOIN detalle_venta dv ON dv.id_venta  = v.id_venta
GROUP BY DATE(v.fecha), u.id_usuario, u.nombre, u.apellido;

-- 3.6 Discrepancias de un conteo físico (RF-05, RF-06)
SELECT p.nombre,
       dc.stock_sistema,
       dc.cantidad_contada,
       dc.cantidad_contada - dc.stock_sistema AS diferencia
FROM detalle_conteo dc
JOIN producto p ON p.id_producto = dc.id_producto
WHERE dc.id_conteo = 1
  AND dc.cantidad_contada <> dc.stock_sistema;

-- 3.7 Historial de ajustes con motivo y responsable (RF-07)
SELECT a.fecha, p.nombre, a.cantidad, m.descripcion AS motivo,
       u.username AS responsable, a.observacion
FROM ajuste_inventario a
JOIN producto p      ON p.id_producto = a.id_producto
JOIN motivo_ajuste m ON m.id_motivo   = a.id_motivo
JOIN usuario u       ON u.id_usuario  = a.id_usuario
ORDER BY a.fecha DESC;

-- ---------------------------------------------------------------------
-- 4. BORRADO DE REGISTROS (DELETE)
-- ---------------------------------------------------------------------

-- 4.1 Anular la venta 2: primero se devuelve el stock, luego se borra.
--     El detalle se elimina en cascada (ON DELETE CASCADE).
UPDATE producto p
JOIN detalle_venta dv ON dv.id_producto = p.id_producto
SET p.stock_actual = p.stock_actual + dv.cantidad
WHERE dv.id_venta = 2;

DELETE FROM venta WHERE id_venta = 2;

-- 4.2 Verificación: la venta y su detalle ya no existen
SELECT * FROM venta;
SELECT * FROM detalle_venta;
