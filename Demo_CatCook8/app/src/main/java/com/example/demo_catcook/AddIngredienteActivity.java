package com.example.demo_catcook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
//Gracias a esta clase se pueden añadir ingredientes al inventario del usuario
public class AddIngredienteActivity extends AppCompatActivity {

    private RecyclerView recyclerViewIngredientes;
    private SearchView searchViewIngredientes;
    private Spinner spinnerUnidadMedida;
    private EditText editTextCantidad;
    private Button buttonGuardar;
    //uso de adaptadores y listas para los ingredientes a mostrar
    private IngredienteAdapter2 ingredienteAdapter;
    private List<Ingrediente> listaIngredientes = new ArrayList<>();
    private Ingrediente ingredienteSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingrediente);
        //Elementos que se mostrarán en la pantalla para interactuar
        recyclerViewIngredientes = findViewById(R.id.recyclerViewIngredientes);
        searchViewIngredientes = findViewById(R.id.searchViewIngredientes);
        spinnerUnidadMedida = findViewById(R.id.spinnerUnidadMedida);
        editTextCantidad = findViewById(R.id.editTextCantidad);
        buttonGuardar = findViewById(R.id.buttonGuardar);

        recyclerViewIngredientes.setLayoutManager(new LinearLayoutManager(this));

        // Cargar lista de ingredientes
        cargarIngredientes();

        // Configurar Spinner con opciones de unidad de medida
        ArrayAdapter<CharSequence> adapterUnidadMedida = ArrayAdapter.createFromResource(
                this, R.array.unidades_medida, android.R.layout.simple_spinner_item);
        adapterUnidadMedida.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnidadMedida.setAdapter(adapterUnidadMedida);

        // Configurar búsqueda de cada ingrediente
        searchViewIngredientes.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ingredienteAdapter.filtrar(newText);
                return true;
            }
        });

        // Botón para guardar ingrediente en el inventario
        buttonGuardar.setOnClickListener(view -> guardarEnInventario());
    }
    //Se cargan los ingredientes disponibles para agregar al inventario
    private void cargarIngredientes() {
        new Thread(() -> {
            try {
                Connection con = ConnectionSingleton.getConnection();
                String query = "SELECT id_ingrediente, nombre, ruta_imagen FROM ingrediente";
                PreparedStatement preparedStatement = con.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery();

                while (resultSet.next()) {
                    Ingrediente ingrediente = new Ingrediente(
                            resultSet.getInt("id_ingrediente"),
                            resultSet.getString("nombre"),
                            resultSet.getString("ruta_imagen"));
                    listaIngredientes.add(ingrediente);
                }

                runOnUiThread(() -> {
                    ingredienteAdapter = new IngredienteAdapter2(listaIngredientes, this::onIngredienteSeleccionado);
                    recyclerViewIngredientes.setAdapter(ingredienteAdapter);
                });

            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al cargar ingredientes",
                        Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    //Segun el ingrediente seleccionado se muestra en la pantalla
    private void onIngredienteSeleccionado(Ingrediente ingrediente) {
        this.ingredienteSeleccionado = ingrediente;
        Toast.makeText(this, "Seleccionado: " + ingrediente.getNombre(), Toast.LENGTH_SHORT).show();
    }
    //Se guarda el ingrediente en el inventario personal del usuario
    private void guardarEnInventario() {
        if (ingredienteSeleccionado == null) {
            Toast.makeText(this, "Seleccione un ingrediente", Toast.LENGTH_SHORT).show();
            return;
        }

        String unidadMedida = spinnerUnidadMedida.getSelectedItem().toString();
        String cantidadTexto = editTextCantidad.getText().toString();
        //verificacion de que se haya ingresado una cantidad
        if (cantidadTexto.isEmpty()) {
            Toast.makeText(this, "Ingrese una cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        double cantidad = Double.parseDouble(cantidadTexto);
        //consulta a la base de datos para guardar el ingrediente en el inventario segun lo ingresado
        new Thread(() -> {
            try {
                Connection con = ConnectionSingleton.getConnection();
                String query = "INSERT INTO inventario_usuario (id_usuario, id_ingrediente, cantidad_disponible," +
                        " unidad_medida) VALUES (?, ?, ?, ?)";
                PreparedStatement preparedStatement = con.prepareStatement(query);
                preparedStatement.setInt(1, obtenerIdUsuario()); // ID del usuario actual
                preparedStatement.setInt(2, ingredienteSeleccionado.getIdIngrediente());
                preparedStatement.setDouble(3, cantidad);
                preparedStatement.setString(4, unidadMedida);

                int rows = preparedStatement.executeUpdate();
                runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(this, "Ingrediente añadido al inventario", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al añadir ingrediente", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error al guardar en inventario", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    //Obtiene el id del usuario con sesion iniciada por medio de un intent
    private int obtenerIdUsuario() {
        int idUsuario = getIntent().getIntExtra("id_usuario", 0);
        Log.d("AddIngredienteActivity", "ID Usuario recibido: " + idUsuario);
        return idUsuario;
    }
}

