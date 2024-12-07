package com.example.demo_catcook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.List;

//Por medio de esta clase se mostraran las valoraciones de cada receta en un recycler view
public class ValoracionAdapter extends RecyclerView.Adapter<ValoracionAdapter.ViewHolder> {
    private List<Valoracion> valoraciones;
    private Context context;

    // Constructor de la clase
    public ValoracionAdapter(Context context, List<Valoracion> valoraciones) {
        this.context = context;
        this.valoraciones = valoraciones;
    }
    //Metodo para inflar el layout de cada item
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_valoracion,
                parent, false);
        return new ViewHolder(view);
    }
    //Metodo para asignar los valores de cada item
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Valoracion valoracion = valoraciones.get(position);

        holder.textViewNombreUsuarioValoracion.setText(valoracion.getNombreUsuario());
        holder.ratingBar.setRating(valoracion.getPuntuacion());
        holder.comentario.setText(valoracion.getComentario());
        // Carga la imagen que tiene cada usuario
        Glide.with(context)
                .load(valoracion.getFotoUsuario())
                .placeholder(R.drawable.ic_usuario)
                .transform(new CircleCrop()) // Imagen por defecto
                .into(holder.imageViewUsuario);
    }
    //Obtener el tamaño de la lista
    @Override
    public int getItemCount() {
        return valoraciones.size();
    }

    //Metodo para actualizar la lista de valoraciones
    public void setValoraciones(List<Valoracion> valoraciones) {
        this.valoraciones = valoraciones;
        notifyDataSetChanged();
    }
    //Clase interna para representar cada item del recycler view
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public RatingBar ratingBar;
        public TextView comentario, textViewNombreUsuarioValoracion;
        ImageView imageViewUsuario;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewUsuario = itemView.findViewById(R.id.imageViewUsuarioValoracion);
            textViewNombreUsuarioValoracion = itemView.findViewById(R.id.textViewNombreUsuarioValoracion);
            ratingBar = itemView.findViewById(R.id.ratingBarItem);
            comentario = itemView.findViewById(R.id.textViewComentario);
        }
    }
}


