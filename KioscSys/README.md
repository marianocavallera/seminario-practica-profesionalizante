# KioscSys

Prototipo del sistema de gestión de stock, costos y ventas para **Kiosco Don Mateo**.
Seminario de Práctica de Informática — Universidad Siglo 21 — Mariano Angel Cavallera.

Desarrollado en **Java 17+ con Swing**, con persistencia en **MySQL 8** mediante **JDBC**, siguiendo la arquitectura **MVC + DAO** definida en el modelo de diseño.

## Funcionalidades del prototipo

| Pantalla | Caso de uso | Rol |
|---|---|---|
| Inicio de sesión | CU-01 Iniciar sesión | Todos |
| Menú principal (barra de menú y botonera según el rol) | — | Todos |
| Registrar venta (lector de código de barras, F10 confirma, Esc cancela) | CU-03 / CU-02 | Vendedor y dueño |
| Gestionar productos (alta, modificación, baja lógica, búsqueda y orden) | CU-05 | Dueño |
| Valorización de inventario y alertas de stock mínimo, con exportación a CSV | CU-09 / CU-11 | Dueño |
| Reporte de ventas del día (ventas por hora, hora pico, ranking de productos, exportación a TXT) | CU-10 | Dueño |

Al confirmar cada venta se genera el ticket en `tickets/ticket-NNNNNN.txt`. Las exportaciones se guardan en `exportaciones/`.

## Estructura

```
src/main/java/kioscsys/
├── KioscSys.java      Punto de entrada (verifica la conexión y abre el login)
├── modelo/            Clases del negocio: Usuario (abstracta), Vendedor, Administrador,
│                      Producto, Categoria, Venta, DetalleVenta, interfaz Valorizable
├── vista/             Pantallas Swing
├── controlador/       Un controlador por caso de uso
├── dao/               Único lugar con sentencias SQL
├── archivo/           Archivos de texto: ArchivoTexto (abstracta, Template Method),
│                      TicketVenta, ExportacionValorizacionCSV, ReporteDiarioTXT
├── conexion/          ConexionBD (Singleton JDBC)
├── excepciones/       StockInsuficienteException, DatosInvalidosException, AutenticacionException
└── util/              Algoritmos de ordenación y búsqueda, formatos, hash SHA-256
sql/kioscsys.sql       Script de creación y carga de la base de datos
sql/ventas_demo_hoy.sql  (Opcional) ventas de ejemplo con la fecha de hoy, para el reporte
config.properties.ejemplo  Modelo del archivo de conexión a MySQL
```

## Cómo ejecutarlo

1. Ejecutar `sql/kioscsys.sql` en MySQL (Navicat, Workbench o consola). Crea la base `kioscsys` con datos de ejemplo.
   Opcional: ejecutar después `sql/ventas_demo_hoy.sql` para tener ventas de hoy en distintos horarios.
2. Copiar `config.properties.ejemplo` como `config.properties` y poner la contraseña del usuario `root` de MySQL. Ese archivo no se sube a GitHub.
3. Abrir la carpeta del proyecto en **Apache NetBeans** (*File → Open Project*). Al ser un proyecto Maven, NetBeans descarga solo el conector de MySQL.
4. Ejecutar con **Run (F6)**.

Usuarios de prueba:

| Usuario | Contraseña | Rol |
|---|---|---|
| `mgomez` | `admin123` | Dueño/Administrador |
| `lperez` | `venta123` | Vendedor |

Códigos de barras para probar una venta: `7790895000997` (gaseosa), `7790580100017` (alfajor), `7790040613707` (agua, con stock bajo el mínimo).
