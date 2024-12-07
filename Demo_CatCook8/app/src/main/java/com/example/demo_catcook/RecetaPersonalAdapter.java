package com.example.demo_catcook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

//Clase que nos ayuda en el manejo del recyclerView en el que se cargan las recetas personales
public class RecetaPersonalAdapter extends RecyclerView.Adapter<RecetaPersonalAdapter.ViewHolder> {

    // Lista de recetas personales a mostrar y el contexto actual
    private List<RecetaPersonal> recetas;
    private Context context;
    private String ingredientes, tiempoPreparacion, dificultad, url;

    // Constructor que inicializa la lista de recetas y el contexto
    public RecetaPersonalAdapter(List<RecetaPersonal> recetas, Context context) {
        this.recetas = recetas;
        this.context = context;
    }
    // Infla el diseño del item y crea un nuevo ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_receta_personal, parent, false);
        return new ViewHolder(view);
    }

    // Enlaza los datos de la receta con las vistas del ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecetaPersonal receta = recetas.get(position);
        // Asignar los datos a las vistas
        holder.nombre.setText(receta.getNombre());
        holder.tiempo.setText(receta.getTiempoPreparacion());
        ingredientes = receta.getIngredientes();
        tiempoPreparacion = receta.getTiempoPreparacion();
        dificultad = receta.getDificultad();
        // Usamos Glide para cargar la imagen de la receta desde su URL
        Glide.with(context)
                .load(receta.getImagenUrl())
                .placeholder(R.drawable.ic_upload_cloud)
                .into(holder.imagen);

        // Defineimos un listener para el clic en el item de cada receta
        holder.itemView.setOnClickListener(v -> {
            // Pasar detalles de la receta a la actividad de detalles
            Intent intent = new Intent(context, RecetaPersonalDetalle.class);
            intent.putExtra("id_receta", receta.getIdReceta());
            intent.putExtra("nombre", receta.getNombre());
            intent.putExtra("descripcion", receta.getDescripcion());
            intent.putExtra("instrucciones", receta.getInstrucciones());
            intent.putExtra("ingredientes", receta.getIngredientes());
            intent.putExtra("tiempoPreparacion", receta.getTiempoPreparacion());
            intent.putExtra("dificultad", receta.getDificultad());
            intent.putExtra("imagenUrl", receta.getImagenUrl());

            // Inicia la actividad de detalles
            context.startActivity(intent);
        });

        // Define un listener para el botón de eliminar receta
        holder.btnEliminar.setOnClickListener(v -> {
            // Muestra un diálogo de confirmación antes de eliminar
            new AlertDialog.Builder(context)
                    .setTitle("Eliminar receta")
                    .setMessage("¿Estás seguro de que deseas eliminar esta receta?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        eliminarRecetaDeBaseDeDatos(receta.getIdReceta(), position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
    // Método para eliminar una receta de la base de datos
    private void eliminarRecetaDeBaseDeDatos(int recetaId, int position) {
        new Thread(() -> {
            try {
                Connection con = ConnectionSingleton.getConnection();
                String query = "DELETE FROM receta_personal WHERE id_receta_personal = ?";
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setInt(1, recetaId);
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    // Remover receta de la lista y actualizar el RecyclerView
                    recetas.remove(position);
                    ((Activity) context).runOnUiThread(() -> notifyItemRemoved(position));
                }

                preparedStatement.close();
                con.close();
            } catch (Exception e) {
                Log.e("RecetaAdapter", "Error al eliminar receta", e);
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return recetas.size();
    }

    // Clase interna para el ViewHolder que almacena las vistas del item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, tiempo;
        ImageView imagen;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.TxtViewNombreRecetaPersonal);
            tiempo = itemView.findViewById(R.id.TxtViewDescripcionRecetaPersonal);
            imagen = itemView.findViewById(R.id.IconImageViewRecetaPersonal);
            btnEliminar = itemView.findViewById(R.id.btnBorrarReceta);
        }
    }
}

