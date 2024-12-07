package com.example.demo_catcook;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//Clase para mostrar el historial de los ingredientes gastados por el usuario
public class historial_inventario extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistorialAdapter adapter;
    private List<HistorialItem> historialList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_inventario);
        //Configuracion del recyclerView
        recyclerView = findViewById(R.id.recyclerViewHistorial);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int idUsuario = getIntent().getIntExtra("id_usuario", 0);

        cargarHistorial(idUsuario);
    }
    //Se obtienen los datos del historial de los ingredientes gastados de la clase HistorialDAO
    private void cargarHistorial(int idUsuario) {
        new Thread(() -> {
            HistorialDAO historialDAO = new HistorialDAO();
            historialList = historialDAO.obtenerHistorial(idUsuario);
            List<HistorialItem> historialConEncabezados = prepararDatosConEncabezados(historialList);
            //Se cargan los datos en el recyclerView
            runOnUiThread(() -> {
                adapter = new HistorialAdapter(this, historialConEncabezados);
                recyclerView.setAdapter(adapter);
            });
        }).start();
    }
    //Se organizan los datos para mostrarlos en el recyclerView
    private List<HistorialItem> prepararDatosConEncabezados(List<HistorialItem> historialItems) {
        List<HistorialItem> organizados = new ArrayList<>();
        String fechaActual = "";

        for (HistorialItem item : historialItems) {
            // Si cambia la fecha, agregamos un encabezado
            if (!item.getFecha().equals(fechaActual)) {
                fechaActual = item.getFecha();
                organizados.add(new HistorialItem(fechaActual)); // Crear encabezado
            }
            organizados.add(item); // Agregar el elemento del historial
        }
        return organizados;
    }

}
