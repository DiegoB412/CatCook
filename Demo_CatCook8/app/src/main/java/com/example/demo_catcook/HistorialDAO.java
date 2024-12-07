package com.example.demo_catcook;

import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
//De aquí se obtienen los datos del historial de los ingredientes gastados de la base de datos
public class HistorialDAO {

    //Se crea el constructor de la clase vacio
    public HistorialDAO() {}

    //Este metodo obtiene una lista del historial de los ingredientes gastados
    public List<HistorialItem> obtenerHistorial(int idUsuario) {
        List<HistorialItem> historialList = new ArrayList<>();
        Connection con = ConnectionSingleton.getConnection();
        //verificacion que la conexion no sea nula
        if (con != null) {
            String query = "SELECT DATE_FORMAT(h.fecha, '%Y-%m-%d') AS fecha, " +
                    "h.cantidad_cambio, " +
                    "h.descripcion, " +
                    "i.nombre AS nombre_ingrediente " +
                    "FROM historial_inventario h " +
                    "JOIN ingrediente i ON h.id_ingrediente = i.id_ingrediente " +
                    "WHERE h.id_usuario = ? " +
                    "ORDER BY h.fecha DESC";
            //se crea la consulta sql
            try (PreparedStatement stmt = con.prepareStatement(query)) {
                stmt.setInt(1, idUsuario);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    historialList.add(new HistorialItem(
                            rs.getString("nombre_ingrediente"),
                            rs.getDouble("cantidad_cambio"),
                            rs.getString("fecha"),
                            rs.getString("descripcion")
                    ));
                }
            } catch (SQLException e) {
                Log.e("HistorialDAO", "Error en la consulta SQL", e);
            } finally {
                try {
                    con.close();
                } catch (SQLException e) {
                    Log.e("HistorialDAO", "Error al cerrar conexión", e);
                }
            }
        } else {
            Log.e("HistorialDAO", "No se pudo establecer la conexión");
        }
        return historialList;
    }
}



