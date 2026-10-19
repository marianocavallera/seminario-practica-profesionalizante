package kioscsys.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import kioscsys.conexion.ConexionBD;
import kioscsys.excepciones.StockInsuficienteException;
import kioscsys.modelo.DetalleVenta;
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
