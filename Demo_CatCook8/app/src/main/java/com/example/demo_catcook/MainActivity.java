package com.example.demo_catcook;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*Por medio de esta clase es posible validar los datos de inicio de sesión de los usuarios
 en la base de datos y dar acceso a las demás funciones de la aplicación*/
public class MainActivity extends AppCompatActivity {
    String str;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Se forza a no integrar el modo oscuro en la aplicación
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        connect();

        //Al presionar el botón crear se inicia la activity VtCrearCuenta
        Button btn = findViewById(R.id.btnCrear);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, VtCrearCuenta.class);
                startActivity(intent);
            }
        });


    }

    //Método para validar los datos de inicio de sesión en la base de datos
    public void InicioSesion(String usuario, String contra) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                Connection con = ConnectionSingleton.getConnection();
                if (con == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Error en la conexión",
                            Toast.LENGTH_SHORT).show());
                    return;
                }

                // Consulta SQL
                String query = "SELECT * FROM usuario WHERE nombre = ? AND contraseña = ?";

                // Preparar la consulta
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, usuario);
                stmt.setString(2, contra);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    // Si encuentra los datos, obtiene la información
                    int id = rs.getInt("id_usuario");
                    String nombre = rs.getString("nombre");
                    String correo = rs.getString("correo_electronico");
                    String preferencias = rs.getString("preferencias_alimenticias");
                    String pass = rs.getString("contraseña");

                    // Enviar los datos a la nueva Activity
                    runOnUiThread(() -> {
                        Intent intent = new Intent(this, VtSesionIniciada.class);
                        intent.putExtra("id_usuario", id);
                        intent.putExtra("nombre", nombre);
                        intent.putExtra("correo", correo);
                        intent.putExtra("preferencias", preferencias);
                        intent.putExtra("contraseña", pass);
                        startActivity(intent);
                    });

                } else {
                    runOnUiThread(() -> mostrarDialogoPersonalizado("Error",
                            "Usuario o contraseña incorrectos"));
                }

            } catch (SQLException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error de SQL"+ e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    //Botón para iniciar el menú de inicio de sesión con los datos ingresados
    public void btnInicio (View View) {
        EditText u = findViewById(R.id.editTextUsu);
        EditText c = findViewById(R.id.editTextContr);

        String us= u.getText().toString();
        String con= c.getText().toString();

        InicioSesion(us,con);

    }
    //conexión a la base de datos
    public void connect(){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(()->{
            try{
                Connection con = ConnectionSingleton.getConnection();
                if(con==null){str= "Error en la conexión"; }
                else{str="Conexión exitosa";}

            }catch(Exception e){
                throw new RuntimeException(e);
            }

            runOnUiThread(() ->{
                try{
                    Thread.sleep(500);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
                Toast.makeText(this,str,Toast.LENGTH_SHORT).show();

            });

        });

    }

    //Método para mostrar un diálogo personalizado de advertencia
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

}