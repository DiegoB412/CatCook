package com.example.demo_catcook;

import android.content.Context;
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

import java.util.List;
//Por medio de esta clase se mostraran los ingredientes de cada receta en un recycler view
public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {
    //Se declara las variables de contexto y la de lista de ingredientes
    private Context context;
    private List<IngredienteReceta> ingredientes;

    //Constructor de la clase
    public IngredienteAdapter(Context context, List<IngredienteReceta> ingredientes) {
        this.context = context;
        this.ingredientes = ingredientes;
    }
    //Metodo para inflar el layout de cada item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }
    //Metodo para asignar los valores de cada item en el recycler view
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IngredienteReceta ingrediente = ingredientes.get(position);
        holder.nombre.setText(ingrediente.getNombre());
        holder.cantidad.setText(String.format("%s %s", ingrediente.getCantidad(), ingrediente.getUnidadMedida()));

        // Cargar imagen usando Glide
        Glide.with(context)
                .load(ingrediente.getRutaImagen())
                .placeholder(R.drawable.ic_act_cambios)
                .into(holder.imagen);
    }
    //Obtener el tamaño de la lista de ingredientes
    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    //Clase interna para representar cada item del recycler view
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, cantidad, unidad;
        ImageView imagen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.txtNombIngredienteReceta);
            cantidad = itemView.findViewById(R.id.txtCantIgrendienteReceta);
            unidad= itemView.findViewById(R.id.txtUniMedidaIngredienteReceta);
            imagen = itemView.findViewById(R.id.IconImageIngredienteReceta);
        }
    }
}



