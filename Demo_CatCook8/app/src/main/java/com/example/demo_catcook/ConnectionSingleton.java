package com.example.demo_catcook;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionSingleton {

    //Variables de conexion
    private static Connection connection;
    private static final String DB_URL = "jdbc:mysql://junction.proxy.rlwy.net:38265/CatCookdb?connectTimeout=50000";
    private static final String USER = "root";
    private static final String PASSWORD = "SlCMivQLuWMwzTKbRWycOOZRbUTCdUZh";

    // Constructor privado para evitar instancias
    private ConnectionSingleton() {}

    //Metodo mediante el cual se conecta a la base de datos
    public static synchronized Connection getConnection() {
        connection= null;
        if (connection == null) {
            try {
                // Cargar el driver
                Class.forName("com.mysql.jdbc.Driver");
                // Crear la conexión
                connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                Log.i("Exito","Conexión creada exitosamente desde singleton.");
            } catch (Exception e) {
                Log.e("Error Singleton", e.getMessage(), e);
            }
        }
        return connection;
    }

    //Cierre de la conexion a la base de datos
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Conexión cerrada correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

