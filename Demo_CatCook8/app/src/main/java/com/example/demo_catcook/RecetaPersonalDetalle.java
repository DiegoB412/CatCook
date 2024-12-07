package com.example.demo_catcook;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

//Detalla la receta personal segun los datos obtenidos de cada receta
public class RecetaPersonalDetalle extends AppCompatActivity {

    private ImageView imgDetalleReceta;
    private TextView txtNombreDetalle, txtDescripcionDetalle,txtIngredientesDetalle, txtInstruccionesDetalle,
            txtTiempoDetalle, txtDificultadDetalle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receta_personal_detalle);

        // Configurar la UI segun los datos necesarios
        imgDetalleReceta = findViewById(R.id.imgDetalleReceta);
        txtNombreDetalle = findViewById(R.id.txtNombreDetalle);
        txtDescripcionDetalle = findViewById(R.id.txtDescripcionDetalle);
        txtIngredientesDetalle= findViewById(R.id.txtIngredientesDetalle);
        txtInstruccionesDetalle = findViewById(R.id.txtInstruccionesDetalle);
        txtTiempoDetalle = findViewById(R.id.txtTiempoDetalle);
        txtDificultadDetalle = findViewById(R.id.txtDificultadDetalle);

        // Obtener datos del intent de la actividad anterior
        Intent intent = getIntent();
        String nombre = intent.getStringExtra("nombre");
        String descripcion = intent.getStringExtra("descripcion");
        String ingredientes= intent.getStringExtra("ingredientes");
        String instrucciones = intent.getStringExtra("instrucciones");
        String tiempoPreparacion = intent.getStringExtra("tiempoPreparacion");
        String dificultad = intent.getStringExtra("dificultad");
        String imagenUrl = intent.getStringExtra("imagenUrl");

        // Mostrar datos en la UI
        txtNombreDetalle.setText(nombre);
        txtDescripcionDetalle.setText(descripcion);
        txtIngredientesDetalle.setText(ingredientes);
        txtInstruccionesDetalle.setText(instrucciones);
        txtTiempoDetalle.setText("Tiempo de preparación: " + tiempoPreparacion);
        txtDificultadDetalle.setText("Dificultad: " + dificultad);

        // Cargar imagen usando Glide
        Glide.with(this).load(imagenUrl).into(imgDetalleReceta);
    }
}