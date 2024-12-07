package com.example.demo_catcook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*Si un usuario no tiene una cuenta existente será redirigido aquí para que pueda crear una*/

public class VtCrearCuenta extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vt_crear_cuenta);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    //Método para agregar una nueva cuenta a la base de datos
    public void AgregarCuenta(String nombre, String correo, String contraseña, String preferencias){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            try{

                // Intentar conectar a la base de datos
                Log.d("AgregarCuenta", "Intentando conectar a la base de datos");
                Connection con = ConnectionSingleton.getConnection();
                if (con == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Error en la conexión", Toast.LENGTH_SHORT).show());
                    return;
                }
                Log.d("AgregarCuenta", "Conexión a la base de datos exitosa, ejecutando consulta");
                String query= "INSERT INTO usuario(nombre, correo_electronico, contraseña, preferencias_alimenticias, id_rol) " +
                              "VALUES (?,?,?,?,?)";

                // Preparar la consulta de los datos necesarios
                PreparedStatement stmt= con.prepareStatement(query);
                stmt.setString(1, nombre );
                stmt.setString(2, correo );
                stmt.setString(3, contraseña );
                stmt.setString(4, preferencias );

                int rowsAffected = stmt.executeUpdate();

                // Verificar si la inserción fue exitosa
                Log.d("AgregarCuenta", "Inserción completada, filas afectadas: " + rowsAffected);
                if (rowsAffected > 0){
                    runOnUiThread(() -> mostrarDialogoPersonalizado("Éxito", "Registro realizado correctamente."));
                    new Handler(Looper.getMainLooper()).postDelayed(this::regresarAMainActivity, 2500);

                }else{
                    runOnUiThread(() -> Toast.makeText(this, "No fue posible agregar los datos", Toast.LENGTH_SHORT).show());
                }

                // Cerrar la conexión y el statement
                stmt.close();
                con.close();

                //Capturar excepciones y mostrar errores en la creacion de la cuenta
            }catch (SQLIntegrityConstraintViolationException e) {
                String errorMessage = e.getMessage();
                runOnUiThread(() -> {
                    if (errorMessage.contains("nombre_UNIQUE")) {
                        mostrarDialogoPersonalizado("Usuario duplicado", "El nombre de usuario ya está registrado.");
                    } else if (errorMessage.contains("correo_electronico_UNIQUE")) {
                        mostrarDialogoPersonalizado("Correo duplicado", "El correo ya está registrado.");
                    } else {
                        mostrarDialogoPersonalizado("Error de datos", "Ya existe un registro con su usuario o correo.");
                    }
                });
            }catch(SQLException e){
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de SQL", Toast.LENGTH_SHORT).show());
            }catch (Exception e) {
                e.printStackTrace();
                Log.e("AgregarCuenta", "Error inesperado: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        });
    }

    //Botón para crear una cuenta con los datos ingresados
    public void btnCrearCuenta(View view) {
        try {
            EditText n = findViewById(R.id.editTextNmb);
            EditText ce = findViewById(R.id.editTextCorreo);
            EditText con = findViewById(R.id.editTextCn);
            EditText p = findViewById(R.id.editTextPrefe);

            String nom = n.getText().toString();
            String cre = ce.getText().toString();
            String cont = con.getText().toString();
            String pre = p.getText().toString();

            if (nom.isEmpty() || cre.isEmpty() || cont.isEmpty() || pre.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            AgregarCuenta(nom, cre, cont, pre);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error en el botón: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    //Método para mostrar un diálogo personalizado de aviso
    private void mostrarDialogoPersonalizado(String titulo, String mensaje) {

        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_custom);
        dialog.setCancelable(true);

        TextView titleView = dialog.findViewById(R.id.dialogTitle);
        TextView messageView = dialog.findViewById(R.id.dialogMessage);
        Button button = dialog.findViewById(R.id.dialogButton);

        titleView.setText(titulo);
        messageView.setText(mensaje);
        button.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    //Método para regresar a la activity principal después de un registro exitoso
    private void regresarAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}