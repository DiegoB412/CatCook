package com.example.demo_catcook;

import static android.graphics.ColorSpace.connect;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static java.security.AccessController.getContext;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.demo_catcook.databinding.ActivityVtSesionIniciadaBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.sql.Connection;
import java.util.List;

import fragment.FragmentInventario;
import fragment.FragmentRecetas;
import fragment.FragmentSoporte;
import fragment.FragmentUsuario;

public class VtSesionIniciada extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private RecetaAdapter adapter;
    ActivityVtSesionIniciadaBinding binding;
    FloatingActionButton fab, fabChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_vt_sesion_iniciada);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Pasar los datos desde la actividad anterior (MainActivity) a la actividad actual (VtSesionIniciada)
        Intent intent = getIntent();
        int id_U= intent.getIntExtra("id_usuario", 0);
        String nombre = intent.getStringExtra("nombre");
        String correo = intent.getStringExtra("correo");
        String preferencias = intent.getStringExtra("preferencias");
        String pass = intent.getStringExtra("contraseña");
        int id_rol = intent.getIntExtra("id_rol", 2);

        Connection con = ConnectionSingleton.getConnection();

        // Crear el Bundle con el id_usuario
        Bundle bundle = new Bundle();
        bundle.putInt("id_usuario", id_U);

        // Configurar binding y fragment inicial
        binding = ActivityVtSesionIniciadaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState == null) {
            FragmentRecetas fragmentRecetas = new FragmentRecetas();
            fragmentRecetas.setArguments(bundle);
            replaceFragment(fragmentRecetas);
        }
        //Configuración de la barra de navegación para cambiar de fragment
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.recetas:
                    selectedFragment = new FragmentRecetas();
                    break;
                case R.id.inventario:
                    selectedFragment = new FragmentInventario();
                    break;
                case R.id.usuario:
                    selectedFragment = new FragmentUsuario();
                    break;
                case R.id.soporte:
                    selectedFragment = new FragmentSoporte();
                    break;
            }
            if (selectedFragment != null) {
                selectedFragment.setArguments(bundle);
                replaceFragment(selectedFragment);
            }
            return true;
        });
        fab = findViewById(R.id.fab);

        fab.setOnClickListener(v -> {
            Intent intent3 = new Intent(VtSesionIniciada.this, AddReceta.class);
            intent3.putExtra("id_usuario", id_U);
            startActivity(intent3);
        });
        //Configuración del botón del ChatBot con IA
        fabChat= findViewById(R.id.fabChat);

        fabChat.setOnClickListener(v -> {
            Intent intentchat = new Intent(VtSesionIniciada.this, ChatBotActivity.class);
            intentchat.putExtra("id_usuario", id_U);
            startActivity(intentchat);
        });

    }
    //Metodo para cambiar de fragment cada vez que se selecciona un item de la barra de navegación
    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText);
        return false;
    }

    //Metodo para pedir permisos de notificaciones al celular
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permiso para notificaciones concedido", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permiso para notificaciones denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectionSingleton.closeConnection();
    }

}