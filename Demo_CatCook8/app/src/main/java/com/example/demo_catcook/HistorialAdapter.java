package com.example.demo_catcook;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
//Adaptador para el recyclerView del historial de los ingredientes gastados
public class HistorialAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<HistorialItem> historialList;
    private Context context;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    @Override
    public int getItemViewType(int position) {
        return historialList.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
    }
    //El constructor del adaptador recibe el context y la lista del historial
    public HistorialAdapter(Context context, List<HistorialItem> historialList) {
        this.context = context;
        this.historialList = historialList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtNombreIngrediente, txtCantidadCambio, txtFecha, txtDescripcion;

        public ViewHolder(View view) {
            super(view);
            txtNombreIngrediente = view.findViewById(R.id.txtNombreIngrediente);
            txtCantidadCambio = view.findViewById(R.id.txtCantidadCambio);
            //txtFecha = view.findViewById(R.id.txtFecha);
            txtDescripcion = view.findViewById(R.id.txtDescripcion);
        }
    }

    //Metodo para crear el viewholder
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_historial, parent, false);
            return new ItemViewHolder(view);
        }
    }
    //Metodo para mostrar los datos en el viewholder
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).txtHeaderDate.setText(historialList.get(position).getFecha());
        } else if (holder instanceof ItemViewHolder) {
            HistorialItem item = historialList.get(position);
            ((ItemViewHolder) holder).txtNombreIngrediente.setText(item.getNombreIngrediente());
            ((ItemViewHolder) holder).txtCantidadCambio.setText(String.valueOf(item.getCantidadCambio()));
            ((ItemViewHolder) holder).txtDescripcion.setText(item.getDescripcion());
        }
    }
    //Contador de elementos en la lista del historial
    @Override
    public int getItemCount() {
        return historialList.size();
    }

    //Clase para el viewholder del encabezado
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView txtHeaderDate;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            txtHeaderDate = itemView.findViewById(R.id.txtHeaderDate);
        }
    }
    //Clase para el viewholder del item
    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombreIngrediente, txtCantidadCambio, txtDescripcion;

        public ItemViewHolder(View itemView) {
            super(itemView);
            txtNombreIngrediente = itemView.findViewById(R.id.txtNombreIngrediente);
            txtCantidadCambio = itemView.findViewById(R.id.txtCantidadCambio);
            txtDescripcion = itemView.findViewById(R.id.txtDescripcion);
        }
    }

}
