package kioscsys.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import kioscsys.conexion.ConexionBD;
import kioscsys.excepciones.StockInsuficienteException;
import kioscsys.modelo.DetalleVenta;
import kioscsys.modelo.LineaVendida;
import kioscsys.modelo.Venta;

/** Acceso a las tablas venta y detalle_venta. */
public class VentaDAO {

    /**
     * Registra la venta completa en una TRANSACCIÓN: encabezado, detalles y
     * descuento de stock se guardan todos juntos o no se guarda nada.
     * Así nunca queda una venta registrada sin haber descontado el stock (RN-02),
     * aunque se corte la luz o falle la conexión a mitad de camino.
     *
     * @return el id de la venta generado por MySQL.
     */
    public int registrar(Venta venta) throws SQLException, StockInsuficienteException {
        Connection con = ConexionBD.getInstancia().getConexion();
        boolean autoCommitOriginal = con.getAutoCommit();
        con.setAutoCommit(false);
        try {
            int idVenta = insertarEncabezado(con, venta);

            String sqlDetalle = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, "
                              + "precio_unitario, costo_unitario) VALUES (?, ?, ?, ?, ?)";
            // El "AND stock_actual >= ?" vuelve a controlar el stock en la base:
            // si otro puesto vendió el producto mientras tanto, no se actualiza ninguna fila.
            String sqlStock = "UPDATE producto SET stock_actual = stock_actual - ? "
                            + "WHERE id_producto = ? AND stock_actual >= ?";

            try (PreparedStatement psDetalle = con.prepareStatement(sqlDetalle);
                 PreparedStatement psStock = con.prepareStatement(sqlStock)) {
                for (DetalleVenta d : venta.getDetalles()) {
                    psDetalle.setInt(1, idVenta);
                    psDetalle.setInt(2, d.getProducto().getIdProducto());
                    psDetalle.setInt(3, d.getCantidad());
                    psDetalle.setBigDecimal(4, d.getPrecioUnitario());
                    psDetalle.setBigDecimal(5, d.getCostoUnitario());
                    psDetalle.executeUpdate();

                    psStock.setInt(1, d.getCantidad());
                    psStock.setInt(2, d.getProducto().getIdProducto());
                    psStock.setInt(3, d.getCantidad());
                    if (psStock.executeUpdate() == 0) {
                        // El stock cambió desde que se cargó el ítem: se informa el valor real.
                        throw new StockInsuficienteException(d.getProducto().getNombre(),
                                leerStock(con, d.getProducto().getIdProducto()), d.getCantidad());
                    }
                }
            }
            con.commit();
            venta.setIdVenta(idVenta);
            return idVenta;
        } catch (SQLException | StockInsuficienteException | RuntimeException e) {
            con.rollback();   // deshace todo lo que se había grabado de esta venta
            throw e;
        } finally {
            con.setAutoCommit(autoCommitOriginal);
        }
    }

    /**
     * Consulta para el reporte de ventas del día (CU-10): todas las líneas
     * vendidas entre las 00:00 de ese día y las 00:00 del siguiente.
     * Se filtra por rango de fechas (y no con DATE(fecha) = ?) para que
     * MySQL pueda usar un índice sobre la columna fecha.
     *
     * Cada fila del ResultSet se convierte en un objeto LineaVendida y se
     * agrega a un ArrayList, porque no se sabe cuántas filas va a devolver.
     */
    public List<LineaVendida> listarLineasDelDia(LocalDate dia) throws SQLException {
        String sql = "SELECT v.id_venta, v.fecha, p.nombre, d.cantidad, d.precio_unitario, d.costo_unitario "
                   + "FROM venta v "
                   + "JOIN detalle_venta d ON d.id_venta = v.id_venta "
                   + "JOIN producto p ON p.id_producto = d.id_producto "
                   + "WHERE v.fecha >= ? AND v.fecha < ? "
                   + "ORDER BY v.fecha, v.id_venta";
        List<LineaVendida> lineas = new ArrayList<>();
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(dia.atStartOfDay()));
            ps.setTimestamp(2, Timestamp.valueOf(dia.plusDays(1).atStartOfDay()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lineas.add(new LineaVendida(
                            rs.getInt("id_venta"),
                            rs.getTimestamp("fecha").toLocalDateTime(),
                            rs.getString("nombre"),
                            rs.getInt("cantidad"),
                            rs.getBigDecimal("precio_unitario"),
                            rs.getBigDecimal("costo_unitario")));
                }
            }
        }
        return lineas;
    }

    private int leerStock(Connection con, int idProducto) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT stock_actual FROM producto WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private int insertarEncabezado(Connection con, Venta venta) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO venta (fecha, id_usuario) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(venta.getFecha()));
            ps.setInt(2, venta.getUsuario().getIdUsuario());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("MySQL no devolvió el id de la venta.");
                }
                return claves.getInt(1);
            }
        }
    }
}
