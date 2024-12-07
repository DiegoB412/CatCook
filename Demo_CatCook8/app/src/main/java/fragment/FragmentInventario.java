package fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.demo_catcook.AddIngredienteActivity;
import com.example.demo_catcook.ConnectionClass;
import com.example.demo_catcook.ConnectionSingleton;
import com.example.demo_catcook.HistorialItem;
import com.example.demo_catcook.InventarioAdapter;
import com.example.demo_catcook.InventarioIngrediente;
import com.example.demo_catcook.R;
import com.example.demo_catcook.historial_inventario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FragmentInventario extends Fragment {

    private RecyclerView recyclerView;
    private InventarioAdapter adapter;
    private List<InventarioIngrediente> ingredientes;
    ImageButton btnAñadir, ImagebtnHistorialInventario;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public FragmentInventario() {
        // Required empty public constructor
    }

    public static FragmentInventario newInstance(String param1, String param2) {
        FragmentInventario fragment = new FragmentInventario();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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
        View view = inflater.inflate(R.layout.fragment_inventario, container, false);

        Intent intent = requireActivity().getIntent();
        int id_U = intent.getIntExtra("id_usuario", 0);

        recyclerView = view.findViewById(R.id.recyclerViewInventario);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ingredientes = obtenerInventarioUsuario(id_U); // Función que obtiene los datos desde la base de datos
        adapter = new InventarioAdapter(getContext(), ingredientes);
        recyclerView.setAdapter(adapter);

        btnAñadir = view.findViewById(R.id.imageButtonAddIngrediente);
        btnAñadir.setOnClickListener(v -> {
            Intent intent2 = new Intent(getActivity(), AddIngredienteActivity.class);
            intent2.putExtra("id_usuario", id_U);
            startActivity(intent2);
        });

        ImagebtnHistorialInventario= view.findViewById(R.id.ImagebtnHistorialInventario);
        ImagebtnHistorialInventario.setOnClickListener(v -> {
            Intent intent1 = new Intent(requireContext(), historial_inventario.class);
            intent1.putExtra("id_usuario", id_U);
            startActivity(intent1);
        });


        return view;
    }

    private List<InventarioIngrediente> obtenerInventarioUsuario(int userId) {
        List<InventarioIngrediente> ingredientes = new ArrayList<>();

        new Thread(() -> {

            try (Connection con = ConnectionSingleton.getConnection();) {
                if (con != null) {

                String query = "SELECT i.id_ingrediente, i.nombre, i.ruta_imagen, " +
                        "inv.cantidad_disponible, inv.unidad_medida " +
                        "FROM inventario_usuario inv " +
                        "JOIN ingrediente i ON inv.id_ingrediente = i.id_ingrediente " +
                        "WHERE inv.id_usuario = ?";
                PreparedStatement statement = con.prepareStatement(query);
                statement.setInt(1, userId);


                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    int idIngrediente = resultSet.getInt("id_ingrediente");
                    String nombre = resultSet.getString("nombre");
                    String rutaImagen = resultSet.getString("ruta_imagen");
                    double cantidadDisponible = resultSet.getDouble("cantidad_disponible");
                    String unidadMedida = resultSet.getString("unidad_medida");

                    ingredientes.add(new InventarioIngrediente(idIngrediente, nombre, cantidadDisponible, unidadMedida, rutaImagen, userId));
                }
                    InventarioIngrediente inventario= new InventarioIngrediente();
                    inventario.setIdUsuario(userId);

                requireActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });

                resultSet.close();
                statement.close();
                con.close();


                }else {
                    Log.e("Connection", "La conexión es nula");
                }

            } catch (SQLException e) {
                    Log.e("SQL Error FragmentInventario", e.getMessage(), e);
                }
        }).start();

        return ingredientes;
    }




}