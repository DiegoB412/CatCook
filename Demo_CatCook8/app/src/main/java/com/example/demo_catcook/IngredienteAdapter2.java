package com.example.demo_catcook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.List;

//Gracias a esta clase se pueden añadir ingredientes al inventario del usuario por medio de una busqueda
public class IngredienteAdapter2 extends RecyclerView.Adapter<IngredienteAdapter2.IngredienteViewHolder> {

    private List<Ingrediente> listaIngredientes;
    private List<Ingrediente> listaFiltrada;
    private Context context;
    private OnIngredienteClickListener listener;

    // Constructor para inicializar la lista
    public IngredienteAdapter2(List<Ingrediente> listaIngredientes, OnIngredienteClickListener listener) {
        this.listaIngredientes = listaIngredientes;
        this.listaFiltrada = new ArrayList<>(listaIngredientes); // Copia para filtrado
        this.listener = listener;
    }
    //Se crea el viewholder para mostrar los ingredientes
    @NonNull
    @Override
    public IngredienteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente_add, parent, false);
        return new IngredienteViewHolder(view);
    }
    //Se muestra la informacion del ingrediente
    @Override
    public void onBindViewHolder(@NonNull IngredienteViewHolder holder, int position) {
        Ingrediente ingrediente = listaFiltrada.get(position);

        // Configurar datos del ingrediente
        holder.textViewNombre.setText(ingrediente.getNombre());
        Glide.with(context)
                .load(ingrediente.getUrlImagen()) // Cargar imagen desde URL
                .placeholder(R.drawable.ic_sync) // Imagen predeterminada
                .into(holder.imageViewIngrediente);

        // Configurar clic en el ingrediente
        holder.itemView.setOnClickListener(v -> listener.onIngredienteClick(ingrediente));
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    // Método para filtrar la lista de ingredientes
    public void filtrar(String texto) {
        listaFiltrada.clear();
        if (texto.isEmpty()) {
            listaFiltrada.addAll(listaIngredientes);
        } else {
            for (Ingrediente ingrediente : listaIngredientes) {
                if (ingrediente.getNombre().toLowerCase().contains(texto.toLowerCase())) {
                    listaFiltrada.add(ingrediente);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Clase interna para el ViewHolder
    static class IngredienteViewHolder extends RecyclerView.ViewHolder {

        TextView textViewNombre;
        ImageView imageViewIngrediente;

        public IngredienteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombreIngrediente);
            imageViewIngrediente = itemView.findViewById(R.id.imageViewIngrediente);
        }
    }

    // Interfaz para manejar clics en los ingredientes
    public interface OnIngredienteClickListener {
        void onIngredienteClick(Ingrediente ingrediente);
    }
}

