package com.example.demo_catcook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
//Gracias a esta clase se muestran los ingredientes del usuario en el recycler view
public class InventarioAdapter extends RecyclerView.Adapter<InventarioAdapter.InventarioViewHolder> {
    private List<InventarioIngrediente> ingredientes;
    private Context context;
    ConnectionClass connectionClass;

    //Constructor de la clase
    public InventarioAdapter(Context context, List<InventarioIngrediente> ingredientes) {
        this.context= context;
        this.ingredientes = ingredientes;
    }
    //Metodo para inflar el layout de cada item
    @NonNull
    @Override
    public InventarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventario, parent, false);
        return new InventarioViewHolder(view);
    }
    //Se asignan los valores de cada item del recycler view
    @Override
    public void onBindViewHolder(@NonNull InventarioViewHolder holder, int position) {
        InventarioIngrediente ingrediente = ingredientes.get(position);

        holder.nombreTextView.setText(ingrediente.getNombre());
        holder.UnidadMedidaTextView.setText(ingrediente.getUnidadMedida());
        holder.cantidadTextView.setText(String.valueOf(ingrediente.getCantidadDisponible()));
        Glide.with(holder.itemView.getContext())
                .load(ingrediente.getRutaImagen())
                .into(holder.imagenImageView);
        //cuando se hace click en el boton de actualizar se actualiza la cantidad
        holder.btnActCantidad.setOnClickListener(v -> {
            double nuevaCantidad;
            try {
                nuevaCantidad = Double.parseDouble(holder.cantidadTextView.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(context, "Ingrese una cantidad válida", Toast.LENGTH_SHORT).show();
                return;
            }
            //Se actualiza la cantidad segun la diferencia entre la nueva y la anterior
            double diferencia = nuevaCantidad - ingrediente.getCantidadDisponible();
            if (diferencia != 0) {
                ingrediente.setCantidadDisponible(nuevaCantidad);
                actualizarCantidadEnBD(ingrediente, nuevaCantidad, diferencia);
            } else {
                Toast.makeText(context, "No se detectaron cambios", Toast.LENGTH_SHORT).show();
            }
        });
        //cuando se hace click en el boton de eliminar se elimina el ingrediente del inventario
        holder.btnBorrarIngrediente.setOnClickListener(v -> {
            mostrarDialogoConfirmacion(v.getContext(), ingrediente.getIdIngrediente(), position);
        });
    }
    //Tamaño de la lista de ingredientes
    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    //Al igual en los demas adaptadores, esta clase es interna para representar cada item del recycler view
    public class InventarioViewHolder extends RecyclerView.ViewHolder {
        TextView nombreTextView, UnidadMedidaTextView;
        EditText cantidadTextView;
        ImageView imagenImageView, btnBorrarIngrediente;
        ImageButton btnActCantidad;

        public InventarioViewHolder(@NonNull View itemView) {
            super(itemView);
            nombreTextView = itemView.findViewById(R.id.TxtNombreIngrediente);
            UnidadMedidaTextView=itemView.findViewById(R.id.txtUnidadMedida);
            cantidadTextView = itemView.findViewById(R.id.editTxtCantidad);
            imagenImageView = itemView.findViewById(R.id.IconImageIngrediente);
            btnBorrarIngrediente= itemView.findViewById(R.id.btnBorrarIngrediente);
            btnActCantidad = itemView.findViewById(R.id.btnActCantidad);
        }
    }

    private void actualizarCantidadEnBD(InventarioIngrediente ingrediente, double nuevaCantidad, double diferencia) {
        new Thread(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection con = connectionClass.CONN();

            if (con != null) {
                try {
                    // Actualizar cantidad en la tabla inventario_usuario
                    String updateQuery = "UPDATE inventario_usuario SET cantidad_disponible = ? WHERE " +
                            "id_ingrediente = ? AND id_usuario = ?";
                    PreparedStatement updateStmt = con.prepareStatement(updateQuery);
                    updateStmt.setDouble(1, nuevaCantidad);
                    updateStmt.setInt(2, ingrediente.getIdIngrediente());
                    updateStmt.setInt(3, ingrediente.getIdUsuario());
                    updateStmt.executeUpdate();

                    // Insertar en la tabla historial_inventario
                    String insertQuery = "INSERT INTO historial_inventario (id_usuario, id_ingrediente, " +
                            "cantidad_cambio, fecha, descripcion) " +
                            "VALUES (?, ?, ?, NOW(), ?)";
                    PreparedStatement insertStmt = con.prepareStatement(insertQuery);
                    insertStmt.setInt(1, ingrediente.getIdUsuario());
                    insertStmt.setInt(2, ingrediente.getIdIngrediente());
                    insertStmt.setDouble(3, diferencia);
                    insertStmt.setString(4, nuevaCantidad == 0 ? "Ingrediente agotado" :
                            (diferencia > 0 ? "Aumento de cantidad" : "Disminución de cantidad"));
                    insertStmt.executeUpdate();

                    insertStmt.close();
                    updateStmt.close();

                    // Si la cantidad es 0, dispara una notificación
                    if (nuevaCantidad == 0) {
                        enviarNotificacionAgotado(ingrediente.getNombre());
                    }

                    ingrediente.setCantidadDisponible(nuevaCantidad);

                    ((Activity) context).runOnUiThread(() -> {
                        notifyDataSetChanged();
                        Toast.makeText(context, "Cantidad actualizada correctamente", Toast.LENGTH_SHORT).show();
                    });

                } catch (SQLException e) {
                    Log.e("SQL Error", e.getMessage(), e);
                    ((Activity) context).runOnUiThread(() ->
                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                    );
                } finally {
                    try {
                        if (con != null) con.close();
                    } catch (SQLException e) {
                        Log.e("Connection Close Error", e.getMessage(), e);
                    }
                }
            } else {
                Log.e("Connection Error", "Conexión a la base de datos es nula");
                ((Activity) context).runOnUiThread(() ->
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
    //Mostrar advertencia para confirmar que se desea eliminar el ingrediente
    private void mostrarDialogoConfirmacion(Context context, int idIngrediente, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmar eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar este ingrediente?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    eliminarIngrediente(idIngrediente, position);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }
    //Eliminar el ingrediente de la base de datos
    public boolean eliminarIngredienteDeDB(int idIngrediente, int idUsuario) {
        Connection tempCon = null;
        try {
            connectionClass = new ConnectionClass();
            tempCon = connectionClass.CONN();
            if (tempCon != null) {
                // Eliminar el ingrediente de la tabla inventario_usuario
                String deleteQuery = "DELETE FROM inventario_usuario WHERE id_ingrediente = ? AND id_usuario = ?";
                PreparedStatement deleteStmt = tempCon.prepareStatement(deleteQuery);
                deleteStmt.setInt(1, idIngrediente);
                deleteStmt.setInt(2, idUsuario);
                int rowsAffected = deleteStmt.executeUpdate();
                deleteStmt.close();

                // Si se eliminó con éxito, registrar el cambio en historial_inventario
                if (rowsAffected > 0) {
                    String insertHistorialQuery = "INSERT INTO historial_inventario (id_usuario, id_ingrediente," +
                            " cantidad_cambio, fecha, descripcion) " +
                            "VALUES (?, ?, ?, NOW(), ?)";
                    PreparedStatement insertHistStmt = tempCon.prepareStatement(insertHistorialQuery);
                    insertHistStmt.setInt(1, idUsuario);
                    insertHistStmt.setInt(2, idIngrediente);
                    insertHistStmt.setDouble(3, 0); // Cambiar cantidad como 0, ya que se eliminó
                    insertHistStmt.setString(4, "Ingrediente agotado");
                    insertHistStmt.executeUpdate();
                    insertHistStmt.close();
                }
                return rowsAffected > 0; // Retorna true si la eliminación fue exitosa
            } else {
                Log.e("DB Error", "No se pudo establecer la conexión con la base de datos.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (tempCon != null) {
                try {
                    tempCon.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    } //Metodo para eliminar el ingrediente de la lista
    private void eliminarIngrediente(int idIngrediente, int position) {
        new Thread(() -> {
            InventarioIngrediente ingrediente = ingredientes.get(position); // Obtener el ingrediente correspondiente
            boolean eliminado = eliminarIngredienteDeDB(idIngrediente, ingrediente.getIdUsuario());

            // Volver al hilo principal para actualizar la interfaz
            ((Activity) context).runOnUiThread(() -> {
                if (eliminado) {
                    ingredientes.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Ingrediente eliminado", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Error al eliminar el ingrediente", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void enviarNotificacionAgotado(String nombreIngrediente) {
        String titulo = "Ingrediente Agotado";
        String mensaje = "El ingrediente '" + nombreIngrediente + "' se ha agotado. Recuerda comprar más.";

        // Verifica si se tiene el permiso para Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{"android.permission.POST_NOTIFICATIONS"}, 100);
                return;
            }
        }
        // Crear el administrador de notificaciones
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Crear el canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String canalId = "canal_agotado";
            NotificationChannel canal = new NotificationChannel(canalId, "Notificaciones de Inventario",
                    NotificationManager.IMPORTANCE_HIGH);
            canal.setDescription("Notificaciones cuando los ingredientes están agotados.");
            notificationManager.createNotificationChannel(canal);
        }

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_agotado")
                .setSmallIcon(R.drawable.ic_notificacion) // icono del proyecto
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Mostrar la notificación
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }



}
