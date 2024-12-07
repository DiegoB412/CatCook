package com.example.demo_catcook;

import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class ConnectionClass {
    protected static String db= "CatCookdb";
    protected static String ip= "junction.proxy.rlwy.net";
    protected static String port="38265";
    protected static String username ="root";
    protected static String password="SlCMivQLuWMwzTKbRWycOOZRbUTCdUZh";
    
    public Connection CONN(){
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString= "jdbc:mysql://"+ip+":"+port+"/"+db+"?connectTimeout=50000";
            conn = DriverManager.getConnection(connectionString, username, password);
            Log.i("Connection", "Conexión establecida desde la clase Connection");
        } catch (SQLException e) {
            Log.e("SQL Error", e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            Log.e("Class Not Found", e.getMessage(), e);
        } catch (Exception e) {
            Log.e("General Error", e.getMessage(), e);
        }
        return conn;
    }

}
