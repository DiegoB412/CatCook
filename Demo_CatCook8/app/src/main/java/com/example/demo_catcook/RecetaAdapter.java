package com.example.demo_catcook;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.bumptech.glide.Glide;

//Mediante esta clase se crea el adaptador para mostrar las recetas en el fragmento
public class RecetaAdapter extends RecyclerView.Adapter<RecetaAdapter.ViewHolder> {
    private List<Receta> recetas;
    private List<Receta> fullRecetasList;
    private Context context;
    private int idUsuario;
    private boolean personal;

    //Constructor para crear el adaptador
    public RecetaAdapter(Context context, List<Receta> recetas, int idUsuario) {
        this.context = context;
        this.recetas= recetas;
        this.fullRecetasList= new ArrayList<>(recetas);
        this.idUsuario = idUsuario;
    }
    //Metodo para recibir la lista completa de recetas
    public void setFullRecetasList(List<Receta> recetaList) {
        this.fullRecetasList = new ArrayList<>(recetaList);
    }
    //Metodo para recibir si la receta es personal o no
    public void recibirBoolean(boolean personal){
        this.personal= personal;
    }

    //Metodo para filtrar las recetas según su nombre
    public void filter(String text) {
        recetas.clear();
        if (text.isEmpty()) {
            recetas.addAll(fullRecetasList);
        } else {
            String searchText = text.toLowerCase();
            List<Receta> filteredList = new ArrayList<>();

            for (Receta receta : fullRecetasList) {
                if (receta.getNombre().toLowerCase().contains(searchText)) {
                    filteredList.add(receta);
                }
            }
            recetas.addAll(filteredList);
        }
        notifyDataSetChanged();
    }
    public void filtrarRecetas(String textoFiltro, int idCategoriaFiltro) {
        List<Receta> recetasFiltradas = new ArrayList<>();

        for (Receta receta : fullRecetasList) {
            boolean coincideTexto = receta.getNombre().toLowerCase().contains(textoFiltro.toLowerCase());
            boolean coincideCategoria = (idCategoriaFiltro == 0 || receta.getIdCategoria() == idCategoriaFiltro);

            if (coincideTexto && coincideCategoria) {
                recetasFiltradas.add(receta);
            }
        }

        recetas.clear();
        recetas.addAll(recetasFiltradas);
        notifyDataSetChanged();
    }


    //Metodo para crear el ViewHolder que contiene la vista de cada receta
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_receta,
                parent, false);
        return new ViewHolder(view);
    }

    //Metodo para enlazar los datos de la receta con la vista
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receta receta = recetas.get(position);

        holder.recetaNombre.setText(receta.getNombre());
        holder.dificultad.setText(receta.getDificultad());
        holder.descripcion.setText(receta.getDescripcion());
        Glide.with(holder.itemView.getContext())
                .load(receta.getUrlImagen())
                .into(holder.iconImage);

        holder.itemView.setOnClickListener(v -> {
            Intent intent;
            if (personal) {
                // Receta personal
                intent = new Intent(context, RecetaPersonalDetalle.class);
                intent.putExtra("id_receta", receta.getId());
                intent.putExtra("id_usuario", idUsuario);
                intent.putExtra("nombre", receta.getNombre());
                intent.putExtra("descripcion", receta.getDescripcion());
                intent.putExtra("instrucciones", receta.getInstrucciones());
                intent.putExtra("ingredientes", receta.getIngredientes());
                intent.putExtra("tiempoPreparacion", receta.getTiempoPreparacion());
                intent.putExtra("dificultad", receta.getDificultad());
                intent.putExtra("imagenUrl", receta.getUrlImagen());
            } else {
                // Receta pública
                intent = new Intent(context, RecetaDetalle.class);
                intent.putExtra("id_receta", receta.getId());
                intent.putExtra("id_usuario", idUsuario);
            }
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recetas.size();
    }

    //Clase interna para el ViewHolder que contiene la vista de cada receta
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView recetaNombre, dificultad, descripcion;
        public ImageView iconImage;

        public ViewHolder(View itemView) {
            super(itemView);
            iconImage= itemView.findViewById(R.id.IconImageView);
            recetaNombre = itemView.findViewById(R.id.TxtViewNombreReceta);
            dificultad= itemView.findViewById(R.id.TxtViewDificultad);
            descripcion= itemView.findViewById(R.id.TxtViewDescripcionReceta);
        }
    }
}