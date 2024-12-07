package fragment;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo_catcook.ConnectionClass;
import com.example.demo_catcook.ConnectionSingleton;
import com.example.demo_catcook.R;
import com.example.demo_catcook.RecetaPersonal;
import com.example.demo_catcook.RecetaPersonalAdapter;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
//Fragmento para mostrar los datos del usuario que inicio sesion (foto, nombre, recetas cargadas...)
public class FragmentUsuario extends Fragment {

    // Constante para identificar la solicitud de selección de imagen
    private static final int SELECT_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private ImageButton imageButtonEditar;
    private Button buttonGuardarNombre;
    private EditText editTextNombreUsuario;
    int id_U;
    // Parámetros que pueden pasarse al fragmento
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;

    public FragmentUsuario() {}//Constructor vacío necesario para los fragmentos

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_usuario, container, false);
        // Inicializa las vistas y obtiene datos del intent
        profileImageView = view.findViewById(R.id.imageUsuario);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Intent intent = requireActivity().getIntent();
        id_U = intent.getIntExtra("id_usuario", 0);

        // Obtiene información del usuario desde el intent
        String nombre = intent.getStringExtra("nombre");
        String correo = intent.getStringExtra("correo");
        String preferencias = intent.getStringExtra("preferencias");
        String pass = intent.getStringExtra("contraseña");

        EditText textNombre = view.findViewById(R.id.editTextNombreUsuario);
        TextView textCorreo = view.findViewById(R.id.txtCorreo);
        TextView textPreferencias = view.findViewById(R.id.txtPreferencias);

        textNombre.setText(nombre);
        textCorreo.setText(correo);
        textPreferencias.setText(preferencias);

        // Obtiene y muestra la URL de la imagen de perfil desde MySQL usando Glide
        obtenerURLImagenDesdeMySQL(urlImagen -> {
            if (urlImagen != null && !urlImagen.isEmpty()) {
                Glide.with(this)
                        .load(urlImagen)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .override(150, 150)
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.ic_usuario);
            }
        });
        // Configura clic para seleccionar una nueva imagen de perfil
        profileImageView.setOnClickListener(v -> seleccionarImagen());
        cargarRecetasPersonales(id_U);

        // Configura la edición del nombre
        editTextNombreUsuario = view.findViewById(R.id.editTextNombreUsuario);
        imageButtonEditar = view.findViewById(R.id.imageButtonEditar);
        buttonGuardarNombre = view.findViewById(R.id.btnGuardarNombreUsuario);

        // Guarda el nuevo nombre en la base de datos MySQL meiante metodos
        imageButtonEditar.setOnClickListener(v -> {
            editTextNombreUsuario.setEnabled(true);
            buttonGuardarNombre.setEnabled(true);
        });

        buttonGuardarNombre.setOnClickListener(v -> {
            String nuevoNombre = editTextNombreUsuario.getText().toString().trim();
            if (!nuevoNombre.isEmpty()) {
                guardarNuevoNombre(nuevoNombre);
            } else {
                mostrarDialogoPersonalizado("Error", "El nombre no puede estar vacío");
            }
        });

        return view;
    }
    // Actualiza el nombre del usuario en la base de datos
    private void guardarNuevoNombre(String nuevoNombre) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                Connection con = ConnectionSingleton.getConnection();
                if (con == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error de conexión", Toast.LENGTH_SHORT).show());
                    return;
                }

                String query = "UPDATE usuario SET nombre = ? WHERE id_usuario = ?";
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, nuevoNombre);
                stmt.setInt(2, id_U);

                int rowsAffected = stmt.executeUpdate();
                requireActivity().runOnUiThread(() -> {
                    if (rowsAffected > 0) {
                        mostrarDialogoPersonalizado("Cambio realizado",
                                "¡El nombre se actualizó con éxito!");
                        editTextNombreUsuario.setEnabled(false);
                        buttonGuardarNombre.setEnabled(false);
                    } else {
                        mostrarDialogoPersonalizado("Error", "No se pudo actualizar el nombre");
                    }
                });

                stmt.close();
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() ->
                        mostrarDialogoPersonalizado("Error",
                                "El nombre que introdujiste no es válido o ya está en uso"));
            }
        });
    }
    // Abre un diálogo personalizado con un mensaje
    private void mostrarDialogoPersonalizado(String titulo, String mensaje) {

        Dialog dialog = new Dialog(requireContext());
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

    // Inicia un intent para seleccionar una imagen
    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, SELECT_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri); // Muestra la imagen seleccionada en el ImageView

            subirImagenACloudinary(imageUri);
        }
    }
    //Datos para acceder a la nube de cloudinary
    private final Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dk1xn70nm",
            "api_key", "392654736561519",
            "api_secret", "8bxQbBKAipydoECnSFbU8Qpr7n0"
    ));
    // Sube la imagen seleccionada a Cloudinary y guarda su URL en MySQL
    private void subirImagenACloudinary(Uri imageUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri); // Usa requireContext()
                Map response = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap());
                String urlImagen = (String) response.get("secure_url");

                guardarURLenMySQL(urlImagen);

            } catch (Exception e) {
                Log.e("Cloudinary", "Error al subir la imagen", e);
            }
        }).start();
    }

    // Guarda la URL de la imagen en la base de datos MySQL
    private void guardarURLenMySQL(String urlImagen) {
        new Thread(() -> {
            try {
                Connection con = ConnectionSingleton.getConnection();
                if (con != null) {
                    String query = "UPDATE usuario SET ruta_imagen = ? WHERE id_usuario = ?";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setString(1, urlImagen);
                    statement.setInt(2, id_U);
                    statement.executeUpdate();

                    con.close();
                }
            } catch (SQLException e) {
                Log.e("MySQL", "Error al guardar la URL en MySQL", e);
            }
        }).start();
    }
    // Obtiene la URL de la imagen desde MySQL y la pasa al callback
    private void obtenerURLImagenDesdeMySQL(Consumer<String> callback) {
        new Thread(() -> {
            String urlImagen = null;
            try {
                Connection con = ConnectionSingleton.getConnection();
                if (con != null) {
                    String query = "SELECT ruta_imagen FROM usuario WHERE id_usuario = ?";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, id_U);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        urlImagen = resultSet.getString("ruta_imagen");
                    }
                    con.close();
                }
            } catch (SQLException e) {
                Log.e("MySQL", "Error al obtener la URL de MySQL", e);
            }

            String finalUrlImagen = urlImagen;
            requireActivity().runOnUiThread(() -> callback.accept(finalUrlImagen));
        }).start();
    }
    // Carga las recetas personales de cada usuario desde MySQL
    private void cargarRecetasPersonales(int idUsuario) {
        new Thread(() -> {
            List<RecetaPersonal> recetas = new ArrayList<>();
            try {
                Connection con = ConnectionSingleton.getConnection();
                if (con != null) {
                    String query = "SELECT id_receta_personal, nombre, descripcion, imagen_url, " +
                            "ingredientes, tiempo_preparacion, dificultad, instrucciones FROM receta_personal " +
                            "WHERE id_usuario = ?";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, idUsuario);

                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        int idReceta = resultSet.getInt("id_receta_personal");
                        String nombre = resultSet.getString("nombre");
                        String descripcion = resultSet.getString("descripcion");
                        String imagenUrl = resultSet.getString("imagen_url");
                        String ingredientes = resultSet.getString("ingredientes");
                        String tiempoPreparacion = resultSet.getString("tiempo_preparacion");
                        String dificultad = resultSet.getString("dificultad");
                        String instrucciones= resultSet.getString("instrucciones");

                        recetas.add(new RecetaPersonal(idReceta, nombre, instrucciones, descripcion,
                                imagenUrl, ingredientes, tiempoPreparacion, dificultad, imagenUrl));
                    }
                    resultSet.close();
                    statement.close();
                    con.close();
                }
            } catch (SQLException e) {
                Log.e("FragmentUsuario", "Error al cargar recetas", e);
            }
            if (recetas.isEmpty()) {
                Log.d("FragmentUsuario", "No hay recetas para mostrar.");
            }
            // Actualiza el RecyclerView con las recetas obtenidas
            getActivity().runOnUiThread(() -> {
                RecyclerView recyclerView = getView().findViewById(R.id.recyclerViewRecetasUsuario);
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(new RecetaPersonalAdapter(recetas, getContext()));
            });
        }).start();
    }
}