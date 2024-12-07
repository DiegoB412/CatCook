package fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.demo_catcook.ConnectionClass;
import com.example.demo_catcook.R;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//Por medio de este fragment el usuario puede comunicarse con el equipo de CatCook o dejar una valoracion
public class FragmentSoporte extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // Componentes de la interfaz de usuario.
    private RatingBar ratingBar;
    private EditText etComentario, etQuejaSugerencia;
    private Button btnEnviarComentario, btnEnviarQueja;

    private String mParam1;
    private String mParam2;

    public FragmentSoporte() {
    }

    public static FragmentSoporte newInstance(String param1, String param2) {
        FragmentSoporte fragment = new FragmentSoporte();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    // Se ejecuta al crear el fragmento, recupera los parámetros si existen.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    //Se encarga de crear y mostrar la vista llenada con parametros al usuario
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_soporte, container, false);

        ratingBar = view.findViewById(R.id.ratingBar);
        etComentario = view.findViewById(R.id.etComentario);
        etQuejaSugerencia = view.findViewById(R.id.etQuejaSugerencia);
        btnEnviarComentario = view.findViewById(R.id.btnEnviarComentario);
        btnEnviarQueja = view.findViewById(R.id.btnEnviarQueja);

        //Son los botones que permiten realizar acciones de valoracion y comentarios mediante los métodos asignados
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());
        btnEnviarQueja.setOnClickListener(v -> enviarCorreo());

        return view;
    }

    //Envia el comentario al servidor y almacena en la base de datos con el metodo guardarComentarioEnBD
    private void enviarComentario() {
        float puntuacion = ratingBar.getRating();
        String comentario = etComentario.getText().toString();

        if (comentario.isEmpty() || puntuacion == 0.0f) {
            Toast.makeText(getContext(), "Por favor, ingresa una puntuación y un comentario.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        guardarComentarioEnBD(puntuacion, comentario);
        Toast.makeText(getContext(), "Comentario enviado. ¡Gracias por tu opinión!",
                Toast.LENGTH_SHORT).show();
    }

    //Inicializado por el metodo enviarComentario() para almacenar en la base de datos
    private void guardarComentarioEnBD(float puntuacion, String comentario) {
        new Thread(() -> {
            ConnectionClass connectionClass = new ConnectionClass();
            Connection con = connectionClass.CONN();

            if (con != null) {
                try {
                    String query = "INSERT INTO comentarios_app (puntuacion, comentario) VALUES (?, ?)";
                    PreparedStatement stmt = con.prepareStatement(query);
                    stmt.setFloat(1, puntuacion);
                    stmt.setString(2, comentario);
                    stmt.executeUpdate();
                    stmt.close();
                    con.close();
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Comentario guardado.",
                            Toast.LENGTH_SHORT).show());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //Se abre la aplicacion de correo instalada en el dispositivo para enviar un correo
    private void enviarCorreo() {
        String quejaSugerencia = etQuejaSugerencia.getText().toString();
        if (quejaSugerencia.isEmpty()) {
            Toast.makeText(getContext(), "Por favor, escribe una queja o sugerencia.", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = "CatCooksoporte@gmail.com";
        String asunto = "Queja/Sugerencia de un usuario";
        String mensaje = "Queja/Sugerencia:\n" + quejaSugerencia;

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, asunto);
        emailIntent.putExtra(Intent.EXTRA_TEXT, mensaje);

        try {
            startActivity(Intent.createChooser(emailIntent, "Enviar correo..."));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No hay aplicaciones de correo instaladas.", Toast.LENGTH_SHORT).show();
        }
    }
}