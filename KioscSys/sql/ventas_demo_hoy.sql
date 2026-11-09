-- =====================================================================
-- KioscSys - Ventas de demostración para el día de HOY (opcional)
-- ---------------------------------------------------------------------
-- Sirve para que el reporte de ventas del día (CU-10) muestre varias
-- franjas horarias en las capturas y en el video. Ejecutarlo DESPUÉS de
-- kioscsys.sql. No modifica los datos de ejemplo de ese script: agrega
-- ventas con la fecha actual y descuenta el stock vendido, igual que lo
-- hace la aplicación (RN-02).
-- =====================================================================

USE kioscsys;
SET NAMES utf8mb4;

START TRANSACTION;

-- Tabla temporal con las ventas a cargar: hora, vendedor, producto y cantidad
CREATE TEMPORARY TABLE demo (
  nro      INT,
  hora     TIME,
  usuario  VARCHAR(30),
  codigo   VARCHAR(20),
  cantidad INT
);

INSERT INTO demo VALUES
  (1, '08:12:00', 'lperez', '7790895000997', 1),   -- gaseosa
  (1, '08:12:00', 'lperez', '7790580100017', 2),   -- alfajores
  (2, '09:40:00', 'lperez', '7791234000011', 1),   -- cigarrillos
  (3, '12:05:00', 'lperez', '7790310981229', 2),   -- papas
  (3, '12:05:00', 'lperez', '7790895000997', 2),
  (4, '12:30:00', 'lperez', '7790580100017', 3),
  (5, '13:15:00', 'lperez', '7791234000011', 2),
  (6, '17:50:00', 'truiz',  '7790580100017', 4),
  (6, '17:50:00', 'truiz',  '7790895000997', 2),
  (7, '18:20:00', 'truiz',  '7791234000011', 1),
  (7, '18:20:00', 'truiz',  '7790310981229', 1),
  (8, '18:45:00', 'truiz',  '7790070411311', 1),   -- yerba
  (9, '19:10:00', 'truiz',  '7790895000997', 1),
  (9, '19:10:00', 'truiz',  '7790580100017', 2),
  (10,'21:30:00', 'mgomez', '7791234000011', 2);

-- Encabezados: una venta por número, con la fecha de hoy
-- (GROUP BY en lugar de DISTINCT: MySQL 8 no permite DISTINCT con un
--  ORDER BY sobre una columna que no está en el SELECT)
INSERT INTO venta (fecha, id_usuario)
SELECT TIMESTAMP(CURDATE(), d.hora), u.id_usuario
FROM demo d JOIN usuario u ON u.username = d.usuario
GROUP BY d.nro, d.hora, u.id_usuario
ORDER BY d.nro;

-- Detalles: se copian el precio y el costo vigentes (dato histórico)
INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_unitario, costo_unitario)
SELECT v.id_venta, p.id_producto, d.cantidad, p.precio_venta, p.costo_unitario
FROM demo d
JOIN usuario  u ON u.username = d.usuario
JOIN venta    v ON v.fecha = TIMESTAMP(CURDATE(), d.hora) AND v.id_usuario = u.id_usuario
JOIN producto p ON p.codigo_barras = d.codigo;

-- Descuento del stock vendido
UPDATE producto p
JOIN (SELECT codigo, SUM(cantidad) AS vendidas FROM demo GROUP BY codigo) t
  ON t.codigo = p.codigo_barras
SET p.stock_actual = p.stock_actual - t.vendidas;

DROP TEMPORARY TABLE demo;
COMMIT;

-- Verificación: ventas de hoy por hora
SELECT HOUR(v.fecha) AS hora, COUNT(DISTINCT v.id_venta) AS tickets,
       SUM(d.cantidad * d.precio_unitario) AS importe
FROM venta v JOIN detalle_venta d ON d.id_venta = v.id_venta
WHERE v.fecha >= CURDATE() AND v.fecha < CURDATE() + INTERVAL 1 DAY
GROUP BY HOUR(v.fecha) ORDER BY hora;
