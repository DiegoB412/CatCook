package com.example.demo_catcook;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Map;

public class AddReceta extends AppCompatActivity {

    private EditText editTextNombre, editTextIngrediente, editTextDescripcion, editTextInstrucciones;
    private Button botonGuardar;
    private ImageButton btnImagenRecetaPersonal;
    private Spinner spinnerDificultad;
    private TextInputEditText editTextTiempoPreparacion;
    private Switch switchVisibilidad;
    String rutaImagen;
    private static final int REQUEST_CODE_IMAGE_PICK = 1;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_receta);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Pasar los datos desde la actividad anterior (MainActivity) a la actividad actual (AddReceta)
        Intent intent = getIntent();
        int id_U= intent.getIntExtra("id_usuario", 0);

        editTextNombre = findViewById(R.id.editTextNombre);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        editTextIngrediente= findViewById(R.id.editTextIngrediente);
        editTextInstrucciones = findViewById(R.id.editTextInstrucciones);
        editTextTiempoPreparacion = findViewById(R.id.editTextTiempoPreparacion);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        botonGuardar = findViewById(R.id.botonGuardar);
        btnImagenRecetaPersonal = findViewById(R.id.btnImagenRecetaPersonal);
        switchVisibilidad = findViewById(R.id.switchVisibilidad);

        //Función para guardar la receta desde el botón guardar
        botonGuardar.setOnClickListener(v -> {
            agregarReceta(id_U);
        });

        //Guardar la imagen elegida por el usuario para la receta
        btnImagenRecetaPersonal.setOnClickListener(v -> {
            Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent2, REQUEST_CODE_IMAGE_PICK);
        });

        //configuracion de la seleccion del tiempo de preparacion
        editTextTiempoPreparacion.setOnClickListener(v -> {
            int hour = 0; // Hora por defecto
            int minute = 0; // Minuto por defecto
            int second = 0; // Segundo por defecto

            TimePickerDialog timePickerDialog = new TimePickerDialog(AddReceta.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            // El segundo es fijo (0) por defecto, pero puedes agregarlo después.
                            String time = String.format("%02d:%02d:%02d", hourOfDay, minute, second);
                            editTextTiempoPreparacion.setText(time);
                        }
                    }, hour, minute, true);
            timePickerDialog.show();
        });



    }
    //Añadir receta a la base de datos con lo introducido por el usuario
    private void agregarReceta(int idUsuario) {

        new Thread(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection con = connectionClass.CONN();

            if (con != null) {
                try {
                    String query = "INSERT INTO receta_personal (nombre, descripcion, tiempo_preparacion, " +
                            "dificultad, ingredientes, id_usuario, imagen_url, instrucciones, visibilidad) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement preparedStatement = con.prepareStatement(query);

                    // Valores que se van a insertar
                    String nombre = editTextNombre.getText().toString().trim();
                    String descripcion= editTextDescripcion.getText().toString().trim();
                    String ingredientes = editTextIngrediente.getText().toString().trim();
                    String instrucciones = editTextInstrucciones.getText().toString().trim();
                    String tiempoPreparacion = editTextTiempoPreparacion.getText().toString().trim();
                    String dificultad= spinnerDificultad.getSelectedItem().toString();
                    String visibilidad = switchVisibilidad.isChecked() ? "publica" : "privada";

                    Log.d("Valor Spinner", "Dificultad seleccionada: " + dificultad);


                    // Validaciones previas
                    if (nombre.isEmpty() || ingredientes.isEmpty() || instrucciones.isEmpty() || tiempoPreparacion.isEmpty() || dificultad.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    // Configurar valores en la consulta
                    preparedStatement.setString(1, nombre);
                    preparedStatement.setString(2, descripcion);
                    preparedStatement.setTime(3, Time.valueOf(tiempoPreparacion));
                    preparedStatement.setString(4, dificultad);
                    preparedStatement.setString(5, ingredientes);
                    preparedStatement.setInt(6, idUsuario);
                    preparedStatement.setString(7, rutaImagen);
                    preparedStatement.setString(8, instrucciones);
                    preparedStatement.setString(9, visibilidad);

                    // Ejecutar consulta despues de las validaciones
                    int rowsAffected = preparedStatement.executeUpdate();
                    runOnUiThread(() -> {
                        if (rowsAffected > 0) {
                            Toast.makeText(this, "Receta agregada exitosamente", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, "Error al agregar la receta", Toast.LENGTH_SHORT).show();
                            Log.d("Datos Insertados", "Nombre: " + nombre + ", Descripción: " + descripcion +
                                    ", Ingredientes: " + ingredientes + ", Tiempo: " + tiempoPreparacion +
                                    ", Dificultad: " + dificultad + ", Usuario ID: " + idUsuario +
                                    ", Imagen URL: " + rutaImagen + ", Instrucciones: " + instrucciones +
                                    ", Visibilidad: " + visibilidad);

                        }
                    });

                    preparedStatement.close();
                    con.close();
                } catch (SQLException e) {
                    Log.e("SQL Error", e.getMessage(), e); // Imprime el error para diagnosticar
                    runOnUiThread(() -> Toast.makeText(this, "Error al ejecutar la consulta", Toast.LENGTH_SHORT).show());
                } catch (IllegalArgumentException e) {
                    Log.e("Time Format Error", e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(this, "El tiempo de preparación debe estar en formato HH:mm:ss", Toast.LENGTH_SHORT).show());
                }
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No se pudo conectar a la base de datos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    //Configuración de la seleccion de la imagen de la receta
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            subirImagenACloudinary(imageUri);
        }
    }
    //Metodo para subir la imagen a cloudinary
    private void subirImagenACloudinary(Uri imageUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Map response = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                rutaImagen = (String) response.get("secure_url");

                runOnUiThread(() -> Toast.makeText(this, "Imagen subida correctamente", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                Log.e("Cloudinary", "Error al subir la imagen", e);
            }
        }).start();
    }
    //Configuración de los datos para acceder a cloudinary
    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dk1xn70nm",
            "api_key", "392654736561519",
            "api_secret", "8bxQbBKAipydoECnSFbU8Qpr7n0"
    ));

}