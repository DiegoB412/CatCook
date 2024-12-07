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

import java.util.List;

//Clase para mostrar las instrucciones de cada receta en el recycler view de detalle
public class InstruccionAdapter extends RecyclerView.Adapter<InstruccionAdapter.InstruccionViewHolder> {
    private List<Instruccion> instrucciones;
    private Context context;

    //Constructor de la clase
    public InstruccionAdapter(List<Instruccion> instrucciones, Context context) {
        this.instrucciones = instrucciones;
        this.context = context;
    }
    //Se infla el layout de cada item
    @NonNull
    @Override
    public InstruccionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_instruccion, parent, false);
        return new InstruccionViewHolder(view);
    }

    //Se asignan los valores de cada item
    @Override
    public void onBindViewHolder(@NonNull InstruccionViewHolder holder, int position) {
        Instruccion instruccion = instrucciones.get(position);
        holder.textoInstruccion.setText("Paso " + instruccion.getPaso() + ": " + instruccion.getTexto());

        Glide.with(context)
                .load(instruccion.getImagenUrl())
                .placeholder(R.drawable.ic_sync) // Imagen predeterminada mientras carga
                .into(holder.imagenInstruccion);
    }
    //Obtener el tamaño de la lista de instrucciones
    @Override
    public int getItemCount() {
        return instrucciones.size();
    }

    //Clase interna para representar cada item del recycler view de las instrucciones
    public static class InstruccionViewHolder extends RecyclerView.ViewHolder {
        TextView textoInstruccion;
        ImageView imagenInstruccion;

        public InstruccionViewHolder(@NonNull View itemView) {
            super(itemView);
            textoInstruccion = itemView.findViewById(R.id.textoInstruccion);
            imagenInstruccion = itemView.findViewById(R.id.imagenInstruccion);
        }
    }
}

