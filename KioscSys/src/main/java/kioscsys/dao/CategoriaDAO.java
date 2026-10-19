package kioscsys.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import kioscsys.conexion.ConexionBD;
import kioscsys.modelo.Categoria;

/** Acceso a la tabla categoria. */
public class CategoriaDAO {

    public List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        Connection con = ConexionBD.getInstancia().getConexion();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_categoria, nombre FROM categoria ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(new Categoria(rs.getInt("id_categoria"), rs.getString("nombre")));
            }
        }
        return categorias;
    }
}
