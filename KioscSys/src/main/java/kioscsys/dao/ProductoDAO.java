package kioscsys.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import kioscsys.conexion.ConexionBD;
import kioscsys.excepciones.DatosInvalidosException;
import kioscsys.modelo.Categoria;
import kioscsys.modelo.Producto;

/**
 * Acceso a la tabla producto (patrón DAO del diseño del TP2).
 * Toda sentencia SQL sobre productos está acá: ni el modelo ni las
 * pantallas contienen SQL.
 */
public class ProductoDAO {

    private static final String SELECT_BASE =
            "SELECT p.id_producto, p.codigo_barras, p.nombre, p.costo_unitario, p.precio_venta, "
          + "p.stock_actual, p.stock_minimo, p.activo, c.id_categoria, c.nombre AS categoria "
          + "FROM producto p JOIN categoria c ON c.id_categoria = p.id_categoria ";

    /** Productos activos, ordenados por nombre. */
    public List<Producto> listarActivos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                SELECT_BASE + "WHERE p.activo = TRUE ORDER BY p.nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(mapear(rs));
            }
        }
        return productos;
    }

    /** Búsqueda por código de barras (lectura con el lector USB, CU-02). */
    public Producto buscarPorCodigo(String codigo) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                SELECT_BASE + "WHERE p.codigo_barras = ? AND p.activo = TRUE")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Alta de un producto nuevo (CU-05). Devuelve el id generado por MySQL. */
    public int insertar(Producto p) throws SQLException, DatosInvalidosException {
        String sql = "INSERT INTO producto (codigo_barras, nombre, id_categoria, costo_unitario, "
                   + "precio_venta, stock_actual, stock_minimo, activo) VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)";
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigoBarras());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoria().getIdCategoria());
            ps.setBigDecimal(4, p.getCostoUnitario());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getStockActual());
            ps.setInt(7, p.getStockMinimo());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    p.setIdProducto(claves.getInt(1));
                }
            }
            return p.getIdProducto();
        } catch (SQLIntegrityConstraintViolationException e) {
            // La columna codigo_barras es UNIQUE: se traduce a un mensaje del negocio.
            throw new DatosInvalidosException("Ya existe un producto con el código " + p.getCodigoBarras() + ".", e);
        }
    }

    /** Modificación de datos comerciales (no del stock, que cambia solo por movimientos). */
    public void actualizar(Producto p) throws SQLException, DatosInvalidosException {
        String sql = "UPDATE producto SET codigo_barras = ?, nombre = ?, id_categoria = ?, "
                   + "costo_unitario = ?, precio_venta = ?, stock_minimo = ? WHERE id_producto = ?";
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getCodigoBarras());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoria().getIdCategoria());
            ps.setBigDecimal(4, p.getCostoUnitario());
            ps.setBigDecimal(5, p.getPrecioVenta());
            ps.setInt(6, p.getStockMinimo());
            ps.setInt(7, p.getIdProducto());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new DatosInvalidosException("Ya existe otro producto con el código " + p.getCodigoBarras() + ".", e);
        }
    }

    /**
     * Baja lógica: el producto se marca inactivo en lugar de borrarse,
     * porque las ventas anteriores lo siguen referenciando (clave foránea).
     */
    public void darDeBaja(int idProducto) throws SQLException {
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE producto SET activo = FALSE WHERE id_producto = ?")) {
            ps.setInt(1, idProducto);
            ps.executeUpdate();
        }
    }

    /** Convierte una fila del ResultSet en un objeto Producto. */
    private Producto mapear(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria(rs.getInt("id_categoria"), rs.getString("categoria"));
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo_barras"),
                rs.getString("nombre"),
                categoria,
                rs.getBigDecimal("costo_unitario"),
                rs.getBigDecimal("precio_venta"),
                rs.getInt("stock_actual"),
                rs.getInt("stock_minimo"),
                rs.getBoolean("activo"));
    }
}
