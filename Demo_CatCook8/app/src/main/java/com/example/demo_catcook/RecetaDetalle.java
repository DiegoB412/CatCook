package com.example.demo_catcook;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

//Actividad para mostrar los detalles de cada una de las recetas despues de ser seleccionadas
public class RecetaDetalle extends AppCompatActivity {
    private Button buttonGuardarValoracion;
    private TextView nombreReceta, descripcionReceta, instruccionesReceta, tiempoPreparacion,
            dificultadReceta;
    private EditText editTextComentario;
    private RatingBar ratingBar;
    private ValoracionAdapter valoracionAdapter;
    private IngredienteAdapter ingredientesAdapter, ingredientesFaltantesAdapter;
    private List<IngredienteReceta> ingredientes = new ArrayList<>();
    private List<IngredienteReceta> ingredientesFaltantes = new ArrayList<>();
    private int idUsuario, recetaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta_detalle);

        // Inicializar vistas
        nombreReceta = findViewById(R.id.nombreReceta);
        descripcionReceta = findViewById(R.id.descripcionReceta);
        instruccionesReceta = findViewById(R.id.instruccionesReceta);
        tiempoPreparacion = findViewById(R.id.tiempoPreparacion);
        dificultadReceta = findViewById(R.id.dificultadReceta);
        editTextComentario = findViewById(R.id.editTextComentario);
        buttonGuardarValoracion = findViewById(R.id.buttonGuardarValoracion);
        ratingBar = findViewById(R.id.ratingBar);

        // Obtener datos del Intent
        Intent intent = getIntent();
        idUsuario = intent.getIntExtra("id_usuario", 0);
        recetaId = intent.getIntExtra("id_receta", -1);

        //Verificar que se haya seleccionado una receta
        if (recetaId != -1) {
            cargarDetallesReceta(recetaId);
            cargarInstrucciones(recetaId);
        }

        // Configurar RecyclerView de valoraciones
        RecyclerView recyclerViewValoraciones = findViewById(R.id.recyclerViewValoraciones);
        recyclerViewValoraciones.setLayoutManager(new LinearLayoutManager(this));
        valoracionAdapter = new ValoracionAdapter(this, new ArrayList<>());
        recyclerViewValoraciones.setAdapter(valoracionAdapter);

        // Configurar RecyclerView de ingredientes
        RecyclerView recyclerViewIngredientes = findViewById(R.id.recyclerViewIngredientes);
        recyclerViewIngredientes.setLayoutManager(new LinearLayoutManager(this));
        ingredientesAdapter = new IngredienteAdapter(this, ingredientes);
        recyclerViewIngredientes.setAdapter(ingredientesAdapter);

        // Configurar RecyclerView de ingredientes faltantes
        RecyclerView recyclerViewIngredientesFaltantes = findViewById(R.id.recyclerViewIngredientesFaltantes);
        recyclerViewIngredientesFaltantes.setLayoutManager(new LinearLayoutManager(this));
        ingredientesFaltantesAdapter = new IngredienteAdapter(this, ingredientesFaltantes);
        recyclerViewIngredientesFaltantes.setAdapter(ingredientesFaltantesAdapter);

        cargarValoraciones(recetaId);
        cargarIngredientes(recetaId);
        cargarIngredientesFaltantes(recetaId, idUsuario);

        Button buttonPrepararReceta = findViewById(R.id.buttonPrepararReceta);
        buttonPrepararReceta.setOnClickListener(v -> prepararReceta(idUsuario, recetaId));

        buttonGuardarValoracion.setOnClickListener(v -> guardarValoracion());
    }

    //Metodo para indicar que se preparó una receta
    private void prepararReceta(int idUsuario, int idReceta) {
        new Thread(() -> {
            Connection connection = null;
            PreparedStatement stmtIngredientes = null;
            PreparedStatement stmtUpdateInventario = null;
            PreparedStatement stmtConversion = null;
            ResultSet resultSet = null;
            ResultSet rsConversion = null;

            try {
                connection = ConnectionSingleton.getConnection();

                // Consulta para obtener los ingredientes necesarios para la receta
                String queryIngredientes =
                        "SELECT ri.id_ingrediente, ri.cantidad AS cantidad_necesaria, " +
                                "iu.cantidad_disponible, " +
                                "iu.unidad_medida AS unidad_inventario, ri.unidad_medida AS unidad_receta, " +
                                "i.nombre " + "FROM receta_ingrediente AS ri " +
                                "LEFT JOIN inventario_usuario AS iu ON ri.id_ingrediente = iu.id_ingrediente " +
                                "AND iu.id_usuario = ? " +"JOIN ingrediente i ON i.id_ingrediente = ri.id_ingrediente "
                                + "WHERE ri.id_receta = ?";

                stmtIngredientes = connection.prepareStatement(queryIngredientes);
                stmtIngredientes.setInt(1, idUsuario);
                stmtIngredientes.setInt(2, idReceta);
                resultSet = stmtIngredientes.executeQuery();

                boolean tieneIngredientesSuficientes = true;
                List<String> ingredientesInsuficientes = new ArrayList<>();
                List<Integer> ingredientesEnCero = new ArrayList<>();

                // Consulta para obtener el factor de conversión
                String queryConversion =
                        "SELECT factor_conversion " +
                                "FROM conversion_unidades " +
                                "WHERE unidad_origen = ? AND unidad_destino = ?";

                while (resultSet.next()) {
                    int idIngrediente = resultSet.getInt("id_ingrediente");
                    double cantidadNecesaria = resultSet.getDouble("cantidad_necesaria");
                    double cantidadDisponible = resultSet.getDouble("cantidad_disponible");
                    String unidadReceta = resultSet.getString("unidad_receta");
                    String unidadInventario = resultSet.getString("unidad_inventario");
                    String nombreIngrediente = resultSet.getString("nombre");

                    double cantidadConvertida = cantidadNecesaria;

                    // Verifica si las unidades son diferentes
                    if (unidadReceta != null && unidadInventario != null &&
                            !unidadReceta.equals(unidadInventario)) {
                        stmtConversion = connection.prepareStatement(queryConversion);
                        stmtConversion.setString(1, unidadReceta);
                        stmtConversion.setString(2, unidadInventario);
                        rsConversion = stmtConversion.executeQuery();

                        if (rsConversion.next()) {
                            double factorConversion = rsConversion.getDouble("factor_conversion");
                            cantidadConvertida = cantidadNecesaria * factorConversion;
                        } else {
                            // Si no hay conversión, marca como insuficiente
                            tieneIngredientesSuficientes = false;
                            ingredientesInsuficientes.add(nombreIngrediente + " (sin conversión disponible)");
                            continue;
                        }
                    }
                    // Verifica si hay suficiente cantidad en el inventario después de la conversión
                    if (cantidadDisponible < cantidadConvertida) {
                        tieneIngredientesSuficientes = false;
                        ingredientesInsuficientes.add(nombreIngrediente);
                    } else {
                        // Resta la cantidad convertida del inventario
                        double nuevaCantidad = cantidadDisponible - cantidadConvertida;

                        // Actualiza el inventario del usuario
                        String queryUpdate = "UPDATE inventario_usuario SET cantidad_disponible = ? " +
                                "WHERE id_usuario = ? AND id_ingrediente = ?";
                        stmtUpdateInventario = connection.prepareStatement(queryUpdate);
                        stmtUpdateInventario.setDouble(1, nuevaCantidad);
                        stmtUpdateInventario.setInt(2, idUsuario);
                        stmtUpdateInventario.setInt(3, idIngrediente);
                        stmtUpdateInventario.executeUpdate();
                        // Verifica si quedó en 0
                        if (nuevaCantidad == 0) {
                            ingredientesEnCero.add(idIngrediente);
                        }
                    }
                }
                if (!tieneIngredientesSuficientes) {
                    // Muestra un mensaje si faltan ingredientes
                    runOnUiThread(() -> {
                        String mensaje = "Ingredientes insuficientes: " + String.join(", ",
                                ingredientesInsuficientes);
                        Toast.makeText(RecetaDetalle.this, mensaje, Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                if (!ingredientesEnCero.isEmpty()) {
                    // Notifica al usuario si algún ingrediente llegó a 0
                    for (int idIngrediente : ingredientesEnCero) {
                        enviarNotificacion(idIngrediente);
                    }
                }
                // Notifica que la receta fue preparada con éxito
                runOnUiThread(() -> {
                    Toast.makeText(RecetaDetalle.this, "¡Receta preparada con éxito!",
                            Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(RecetaDetalle.this, "Error al preparar la receta",
                            Toast.LENGTH_LONG).show();
                });
            } finally {
                try {
                    if (rsConversion != null) rsConversion.close();
                    if (resultSet != null) resultSet.close();
                    if (stmtIngredientes != null) stmtIngredientes.close();
                    if (stmtUpdateInventario != null) stmtUpdateInventario.close();
                    if (stmtConversion != null) stmtConversion.close();
                    if (connection != null) connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    //Metodo para enviar una notificación como recordatorio
    private void enviarNotificacion(int idIngrediente) {
        new Thread(() -> {
            Connection connection = null;
            PreparedStatement stmt = null;
            ResultSet resultSet = null;

            try {
                connection = ConnectionSingleton.getConnection();

                // Consulta para obtener el nombre del ingrediente
                String query = "SELECT nombre FROM ingrediente WHERE id_ingrediente = ?";
                stmt = connection.prepareStatement(query);
                stmt.setInt(1, idIngrediente);
                resultSet = stmt.executeQuery();

                if (resultSet.next()) {
                    String nombreIngrediente = resultSet.getString("nombre");

                    // Crear la notificación
                    NotificationManager notificationManager = (NotificationManager)
                            getSystemService(Context.NOTIFICATION_SERVICE);
                    String channelId = "inventario_channel";

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationChannel channel = new NotificationChannel(
                                channelId, "Inventario", NotificationManager.IMPORTANCE_HIGH);
                        notificationManager.createNotificationChannel(channel);
                    }

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_notificacion)
                            .setContentTitle("Inventario agotado")
                            .setContentText("El ingrediente " + nombreIngrediente + " se ha agotado.")
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setAutoCancel(true);

                    notificationManager.notify(idIngrediente, builder.build());
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) resultSet.close();
                    if (stmt != null) stmt.close();
                    if (connection != null) connection.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
    //Metodo para cargar los detalles de la receta segun el id
    private void cargarDetallesReceta(int recetaId) {
        new Thread(() -> {
            try (Connection con = ConnectionSingleton.getConnection()) {
                String query = "SELECT nombre, descripcion, tiempo_preparación, dificultad FROM receta " +
                        "WHERE id_receta = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, recetaId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String descripcion = rs.getString("descripcion");
                    String tiempo = rs.getString("tiempo_preparación");
                    String dificultad = rs.getString("dificultad");

                    runOnUiThread(() -> {
                        nombreReceta.setText(nombre);
                        descripcionReceta.setText(descripcion);
                        tiempoPreparacion.setText("Tiempo de preparación: " + tiempo);
                        dificultadReceta.setText("Dificultad: " + dificultad);
                    });
                }
            } catch (SQLException e) {
                Log.e("RecetaDetalle", "Error al cargar detalles de la receta", e);
            }
        }).start();
    }
    //Metodo para cargar las instrucciones de la receta desde la tabla instrucciones_imagenes
    private void cargarInstrucciones(int recetaId) {
        new Thread(() -> {
            List<Instruccion> instrucciones = new ArrayList<>();
            try (Connection con = ConnectionSingleton.getConnection()) {
                String query = "SELECT paso, texto_instruccion, imagen_url FROM instrucciones_imagenes " +
                        "WHERE id_receta = ? ORDER BY paso";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, recetaId);

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int paso = rs.getInt("paso");
                    String texto = rs.getString("texto_instruccion");
                    String imagen = rs.getString("imagen_url");
                    instrucciones.add(new Instruccion(paso, texto, imagen));
                }
            } catch (SQLException e) {
                Log.e("RecetaDetalle", "Error al cargar instrucciones", e);
            }

            runOnUiThread(() -> {
                RecyclerView recyclerView = findViewById(R.id.recyclerViewInstrucciones);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(new InstruccionAdapter(instrucciones, this));
            });
        }).start();
    }
    //Metodo para cargar los ingredientes necesarios para preparar la receta
    private void cargarIngredientes(int recetaId) {
        new Thread(() -> {
            try (Connection con = ConnectionSingleton.getConnection()) {
                String query = "SELECT i.nombre, i.ruta_imagen, ri.cantidad, ri.unidad_medida " +
                        "FROM receta_ingrediente ri " +
                        "JOIN ingrediente i ON ri.id_ingrediente = i.id_ingrediente " +
                        "WHERE ri.id_receta = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, recetaId);

                ResultSet rs = stmt.executeQuery();
                ingredientes.clear();
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String rutaImagen = rs.getString("ruta_imagen");
                    double cantidad = rs.getDouble("cantidad");
                    String unidadMedida = rs.getString("unidad_medida");
                    ingredientes.add(new IngredienteReceta(nombre, cantidad, unidadMedida, rutaImagen));
                }

                runOnUiThread(() -> ingredientesAdapter.notifyDataSetChanged());
            } catch (SQLException e) {
                Log.e("RecetaDetalle", "Error al cargar ingredientes", e);
            }
        }).start();
    }
    //Consulta el inventario del usuario para saber los ingredientes faltantes según la receta
    private void cargarIngredientesFaltantes(int recetaId, int usuarioId) {
        new Thread(() -> {
            try (Connection con = ConnectionSingleton.getConnection()) {
                // Consulta para obtener los ingredientes y sus datos relevantes
                String query = "SELECT i.nombre, i.ruta_imagen, " +
                        "ri.cantidad AS cantidad_receta, ri.unidad_medida AS unidad_receta, " +
                        "IFNULL(iu.cantidad_disponible, 0) AS cantidad_inventario, iu.unidad_medida AS unidad_inventario " +
                        "FROM receta_ingrediente ri " +
                        "JOIN ingrediente i ON ri.id_ingrediente = i.id_ingrediente " +
                        "LEFT JOIN inventario_usuario iu ON ri.id_ingrediente = iu.id_ingrediente AND iu.id_usuario = ? " +
                        "WHERE ri.id_receta = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, usuarioId);
                stmt.setInt(2, recetaId);

                ResultSet rs = stmt.executeQuery();
                ingredientesFaltantes.clear();

                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String rutaImagen = rs.getString("ruta_imagen");
                    double cantidadReceta = rs.getDouble("cantidad_receta");
                    String unidadReceta = rs.getString("unidad_receta");
                    double cantidadInventario = rs.getDouble("cantidad_inventario");
                    String unidadInventario = rs.getString("unidad_inventario");

                    // Obtener la cantidad disponible convertida
                    double cantidadDisponibleConvertida = obtenerCantidadConvertida(con, cantidadInventario,
                            unidadInventario, unidadReceta);

                    // Calcular los ingredientes faltantes
                    double cantidadFaltante = cantidadReceta - cantidadDisponibleConvertida;
                    if (cantidadFaltante > 0) {
                        ingredientesFaltantes.add(new IngredienteReceta(nombre, cantidadFaltante,
                                unidadReceta, rutaImagen));
                    }
                }
                // Verificar si no hay ingredientes faltantes
                if (ingredientesFaltantes.isEmpty()) {
                    ingredientesFaltantes.add(new IngredienteReceta(
                            "No falta ningún ingrediente", // Nombre
                            0,                             // Cantidad
                            "",                            // Unidad de medida
                            null                           // Ruta de imagen
                    ));
                }
                runOnUiThread(() -> ingredientesFaltantesAdapter.notifyDataSetChanged());
            } catch (SQLException e) {
                Log.e("RecetaDetalle", "Error al cargar ingredientes faltantes", e);
            }
        }).start();
    }

    private double obtenerCantidadConvertida(Connection con, double cantidad, String unidadActual, String unidadObjetivo) {
        if (unidadActual == null || unidadObjetivo == null || unidadActual.equals(unidadObjetivo)) {
            return cantidad;
        }

        try {
            // Consulta para obtener el factor de conversión
            String queryConversion = "SELECT factor_conversion FROM conversion_unidades " +
                    "WHERE unidad_origen = ? AND unidad_destino = ?";
            PreparedStatement stmtConversion = con.prepareStatement(queryConversion);
            stmtConversion.setString(1, unidadActual);
            stmtConversion.setString(2, unidadObjetivo);

            ResultSet rsConversion = stmtConversion.executeQuery();
            if (rsConversion.next()) {
                double factorConversion = rsConversion.getDouble("factor_conversion");
                return cantidad * factorConversion;
            }

            // Si no se encuentra una conversión, devolver cantidad original
            Log.w("RecetaDetalle", "No se encontró conversión entre " + unidadActual + " y " + unidadObjetivo);
            return cantidad;
        } catch (SQLException e) {
            Log.e("RecetaDetalle", "Error al obtener factor de conversión", e);
            return cantidad; // Devolver cantidad original en caso de error
        }
    }
    //Metodo para cargar las valoraciones de la receta
    private void cargarValoraciones(int recetaId) {
        new Thread(() -> {
            try (Connection con = ConnectionSingleton.getConnection()) {
                String query = "SELECT v.puntuacion, v.comentario, u.nombre, u.ruta_imagen " +
                        "FROM valoracion v " +
                        "JOIN usuario u ON v.id_usuario = u.id_usuario " +
                        "WHERE v.id_receta = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, recetaId);

                ResultSet rs = stmt.executeQuery();
                List<Valoracion> valoraciones = new ArrayList<>();
                while (rs.next()) {
                    float puntuacion = rs.getFloat("puntuacion");
                    String comentario = rs.getString("comentario");
                    String nombreUsuario = rs.getString("nombre");
                    String rutaImagenUsuario = rs.getString("ruta_imagen");
                    valoraciones.add(new Valoracion(puntuacion, comentario, nombreUsuario, rutaImagenUsuario));
                }
                runOnUiThread(() -> {
                    valoracionAdapter.setValoraciones(valoraciones);
                    valoracionAdapter.notifyDataSetChanged();
                });
            } catch (SQLException e) {
                Log.e("RecetaDetalle", "Error al cargar valoraciones", e);
            }
        }).start();
    }
    //Metodo para guardar la valoración de un usuario
    private void guardarValoracion() {
        String comentario = editTextComentario.getText().toString().trim();
        float puntuacion = ratingBar.getRating();
        if (comentario.isEmpty() || puntuacion == 0) {
            Toast.makeText(this, "Debe ingresar un comentario y una puntuación", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try (Connection con = ConnectionSingleton.getConnection()) {
                String query = "INSERT INTO valoracion (id_usuario, id_receta, puntuacion, comentario) VALUES (?, ?, ?, ?)";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setInt(1, idUsuario);
                stmt.setInt(2, recetaId);
                stmt.setFloat(3, puntuacion);
                stmt.setString(4, comentario);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Valoración guardada exitosamente", Toast.LENGTH_SHORT).show();
                        cargarValoraciones(recetaId); // Actualizar la lista de valoraciones
                    });
                }
            } catch (SQLException e) {
                Log.e("RecetaDetalle", "Error al guardar valoración", e);
            }
        }).start();
    }
}
